package com.commercetools.helper.mapper.impl.payment;

import io.sphere.sdk.carts.LineItem;
import io.sphere.sdk.models.EnumValue;
import io.sphere.sdk.models.LocalizedEnumValue;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.products.ProductVariant;
import io.sphere.sdk.products.attributes.Attribute;
import io.sphere.sdk.products.attributes.AttributeAccess;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ProductNameMapperTest {

    private ProductNameMapper productNameMapper;
    private String testAttrName = "abc";

    @Before
    public void setUp() {
        productNameMapper = new ProductNameMapper();
        productNameMapper.setPrefixProductNameWithAttr(testAttrName);
    }

    @Test
    public void getPaypalItemName_shouldReturnProductNameWithoutPrefix_whenPrefixProductNameWithAttrIsNotSet() {
        // preparation
        productNameMapper.setPrefixProductNameWithAttr(null);
        LineItem mockItem = Mockito.mock(LineItem.class);
        Mockito.when(mockItem.getName()).thenReturn(
                LocalizedString.of(Locale.GERMANY, "product001"));
        List<Locale> localeList = Stream.of(Locale.GERMANY).collect(Collectors.toList());

        // test
        String paypalItemName = productNameMapper.getPaypalItemName(mockItem, localeList);

        // assertions
        assertEquals(paypalItemName, "product001");
    }

    @Test
    public void getPaypalItemName_shouldReturnProductNameWithPrefix_whenAttributeIsTypeString() {
        // preparation
        LineItem mockItem = Mockito.mock(LineItem.class);
        ProductVariant mockVariant = Mockito.mock(ProductVariant.class);
        Attribute attribute = Attribute.of(testAttrName, AttributeAccess.ofString(), "AttrVal");
        Mockito.when(mockVariant.getAttribute(Mockito.anyString())).thenReturn(attribute);
        Mockito.when(mockItem.getVariant()).thenReturn(mockVariant);
        Mockito.when(mockItem.getName()).thenReturn(
                LocalizedString.of(Locale.GERMANY, "product001"));
        List<Locale> localeList = Stream.of(Locale.GERMANY).collect(Collectors.toList());

        // test
        String paypalItemName = productNameMapper.getPaypalItemName(mockItem, localeList);

        // assertions
        assertEquals(paypalItemName, "AttrVal product001");
    }

    @Test
    public void getPaypalItemName_shouldReturnProductNameWithAvailableLocale_whenGivenLocaleListHasNoMatch() {
        // preparation
        LineItem mockItem = Mockito.mock(LineItem.class);
        ProductVariant mockVariant = Mockito.mock(ProductVariant.class);
        Attribute attribute = Attribute.of(testAttrName, AttributeAccess.ofString(), "AttrVal");
        Mockito.when(mockVariant.getAttribute(Mockito.anyString())).thenReturn(attribute);
        Mockito.when(mockItem.getVariant()).thenReturn(mockVariant);
        Mockito.when(mockItem.getName()).thenReturn(
                LocalizedString.of(Locale.GERMANY, "product001"));
        List<Locale> localeList = Stream.of(Locale.UK).collect(Collectors.toList());

        // test
        String paypalItemName = productNameMapper.getPaypalItemName(mockItem, localeList);

        // assertions
        assertEquals(paypalItemName, "AttrVal product001");
    }

    @Test
    public void getPaypalItemName_shouldReturnProductNameWithPrefix_whenAttributeIsTypeLocalizedString() {
        // preparation
        LineItem mockItem = Mockito.mock(LineItem.class);
        ProductVariant mockVariant = Mockito.mock(ProductVariant.class);
        Attribute attribute = Attribute.of(testAttrName, AttributeAccess.ofLocalizedString(),
                LocalizedString.of(Locale.GERMANY, "AttrVal"));
        Mockito.when(mockVariant.getAttribute(Mockito.anyString())).thenReturn(attribute);
        Mockito.when(mockItem.getVariant()).thenReturn(mockVariant);
        Mockito.when(mockItem.getName()).thenReturn(
                LocalizedString.of(Locale.GERMANY, "product001"));
        List<Locale> localeList = Stream.of(Locale.GERMANY).collect(Collectors.toList());

        // test
        String paypalItemName = productNameMapper.getPaypalItemName(mockItem, localeList);

        // assertions
        assertEquals(paypalItemName, "AttrVal product001");
    }

    @Test
    public void getPaypalItemName_shouldReturnProductNameWithPrefix_whenAttributeIsTypeEnum() {
        // preparation
        LineItem mockItem = Mockito.mock(LineItem.class);
        ProductVariant mockVariant = Mockito.mock(ProductVariant.class);
        Attribute attribute = Attribute.of(testAttrName, AttributeAccess.ofEnumValue(),
                EnumValue.of("someKey", "AttrVal"));
        Mockito.when(mockVariant.getAttribute(Mockito.anyString())).thenReturn(attribute);
        Mockito.when(mockItem.getVariant()).thenReturn(mockVariant);
        Mockito.when(mockItem.getName()).thenReturn(
                LocalizedString.of(Locale.GERMANY, "product001"));
        List<Locale> localeList = Stream.of(Locale.GERMANY).collect(Collectors.toList());

        // test
        String paypalItemName = productNameMapper.getPaypalItemName(mockItem, localeList);

        // assertions
        assertEquals(paypalItemName, "AttrVal product001");
    }

    @Test
    public void getPaypalItemName_shouldReturnProductNameWithPrefix_whenAttributeIsTypeLocalizedEnum() {
        // preparation
        LineItem mockItem = Mockito.mock(LineItem.class);
        ProductVariant mockVariant = Mockito.mock(ProductVariant.class);
        Attribute attribute = Attribute.of(testAttrName, AttributeAccess.ofLocalizedEnumValue(),
                LocalizedEnumValue.of("someKey", LocalizedString.of(Locale.GERMANY, "AttrVal")));
        Mockito.when(mockVariant.getAttribute(Mockito.anyString())).thenReturn(attribute);
        Mockito.when(mockItem.getVariant()).thenReturn(mockVariant);
        Mockito.when(mockItem.getName()).thenReturn(
                LocalizedString.of(Locale.GERMANY, "product001"));
        List<Locale> localeList = Stream.of(Locale.GERMANY).collect(Collectors.toList());

        // test
        String paypalItemName = productNameMapper.getPaypalItemName(mockItem, localeList);

        // assertions
        assertEquals(paypalItemName, "AttrVal product001");
    }
}
