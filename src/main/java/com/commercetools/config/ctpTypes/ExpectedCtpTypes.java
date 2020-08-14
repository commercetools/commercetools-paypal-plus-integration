package com.commercetools.config.ctpTypes;

import io.sphere.sdk.types.Type;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Stream;

import static io.sphere.sdk.json.SphereJsonUtils.readObjectFromResource;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

/**
 * Reading directory content from packed resources is a sophisticated task, thus we define the expected CTP {@link Type}
 * keys/file names here.
 * <p>
 * This class setting should be consistent with {@link #PATH_TO_TYPE_MOCKS} directory content.
 */
public enum ExpectedCtpTypes {

    // these values are iterated using #getAllExpectedTypes()
    PAYMENT_PAYPAL("payment-paypal"),
    PAYPAL_PLUS_INTERACTION_NOTIFICATION("paypal-plus-interaction-notification"),
    PAYPAL_PLUS_INTERACTION_REQUEST("paypal-plus-interaction-request"),
    PAYPAL_PLUS_INTERACTION_RESPONSE("paypal-plus-interaction-response");

    /**
     * Resource path where expected CTP types JSON mocks are located
     */
    static final String PATH_TO_TYPE_MOCKS = "ctp/types/";
    static final String CTP_TYPE_MOCK_EXTENSION = ".json";

    final private String key;

    ExpectedCtpTypes(String key) {
        this.key = key;
    }

    /**
     * @return CTP {@link Type} name. This value must be consistent with the resource file name (without extension)
     * and parsed {@link Type#getKey()} value.
     */
    public String getKey() {
        return key;
    }

    /**
     * @return relative resource path to the CTP {@link Type} mock
     */
    String getResourcePath() {
        return PATH_TO_TYPE_MOCKS + key + CTP_TYPE_MOCK_EXTENSION;
    }

    /**
     * Read and convert the associated resource to CTP {@link Type}
     *
     * @return parsed to {@link Type} associated resource
     */
    private Type getAssociatedType() {
        return readObjectFromResource(getResourcePath(), Type.class);
    }

    /**
     * @return array of all expected CTP {@link Type} enums required by the application.
     */
    static ExpectedCtpTypes[] getAllExpectedTypes() {
        return ExpectedCtpTypes.class.getEnumConstants();
    }

    /**
     * @return set of all expected CTP types read from the embed resources
     */
    @Nonnull
    public static Set<Type> getExpectedCtpTypesFromResources() {
        return unmodifiableSet(Stream.of(getAllExpectedTypes())
                .map(ExpectedCtpTypes::getAssociatedType)
                .collect(toSet()));
    }
}
