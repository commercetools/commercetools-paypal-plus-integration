package com.commercetools.helper.mapper.impl.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import io.sphere.sdk.carts.LineItem;
import io.sphere.sdk.models.EnumValue;
import io.sphere.sdk.models.LocalizedEnumValue;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.products.ProductVariant;
import io.sphere.sdk.products.attributes.Attribute;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class ProductNameMapper {

    protected String prefixProductNameWithAttr;

    @Value("${prefixProductNameWithAttr:#{null}}")
    public void setPrefixProductNameWithAttr(String prefixProductNameWithAttr) {
        this.prefixProductNameWithAttr = prefixProductNameWithAttr;
    }

    /**
     * @param localizedString
     * @param locales
     * @return ctp product name String for matching locale
     */
    public String getCtpProductName(@Nonnull LocalizedString localizedString, @Nonnull List<Locale> locales){
        String productName = localizedString.get(locales);
        if (StringUtils.isBlank(productName)) {
            Iterator<Locale> localeIterator = localizedString.getLocales().iterator();
            if (localeIterator.hasNext()) {
                Locale fallBackLocale = localeIterator.next();
                productName = localizedString.get(fallBackLocale);
            }
        }
        return productName;
    }

    /**
     * @param lineItem
     * @param locales
     * @return productName String that will be passed to paypal
     */
    public String getPaypalItemName(@Nonnull LineItem lineItem, @Nonnull List<Locale> locales) {
        String productName = this.getCtpProductName(lineItem.getName(), locales);

        if (StringUtils.isNotBlank(prefixProductNameWithAttr)) {
            String prefix = getPrefixForProductName(lineItem, locales);
            if (StringUtils.isNotBlank(prefix)) {
                return prefix + StringUtils.SPACE + productName;
            }
        }
        return productName;
    }

    private String getPrefixForProductName(@Nonnull LineItem lineItem, @Nonnull List<Locale> locales) {
        String prefix = "";
        ProductVariant variant = lineItem.getVariant();
        if (Objects.nonNull(variant) && Objects.nonNull(variant.getAttributes())) {
            Attribute attribute = variant.getAttribute(prefixProductNameWithAttr);
            if (Objects.nonNull(attribute)) {
                prefix = extractLabelValue(attribute, locales);
            }
        }
        return prefix;
    }

    private String extractLabelValue(@Nonnull Attribute attribute, @Nonnull List<Locale> locales) {
        // set[] attributes are discarded
        if (attribute.getValueAsJsonNode() instanceof ArrayNode) {
            return "";
        }

        return getAttributeLabelValue(attribute, locales);
    }

    private String getAttributeLabelValue(@Nonnull final Attribute attribute, @Nonnull List<Locale> locales) {
        JsonNode valueJsonNode = attribute.getValueAsJsonNode();
        if (Objects.nonNull(valueJsonNode) &&
                valueJsonNode.getNodeType() == JsonNodeType.OBJECT && valueJsonNode.hasNonNull("label")) {
            //either enum or lenum
            JsonNode labelJsonNode = valueJsonNode.get("label");
            if (labelJsonNode.getNodeType() == JsonNodeType.OBJECT) {
                // lenum
                LocalizedEnumValue localizedEnumValue = attribute.getValueAsLocalizedEnumValue();
                return localizedEnumValue.getLabel().get(locales);
            } else {
                // enum
                EnumValue enumValue = attribute.getValueAsEnumValue();
                return enumValue.getLabel();
            }
        } else if (Objects.nonNull(valueJsonNode) &&
                valueJsonNode.getNodeType() == JsonNodeType.OBJECT && !valueJsonNode.has("typeId")) {
            //lText
            LocalizedString localizedString = attribute.getValueAsLocalizedString();
            return localizedString.get(locales);
        } else {
            return attribute.getValueAsString();
        }
    }
}
