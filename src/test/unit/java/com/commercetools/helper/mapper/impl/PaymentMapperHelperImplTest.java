package com.commercetools.helper.mapper.impl;

import com.commercetools.helper.formatter.impl.PaypalPlusFormatterImpl;
import com.commercetools.helper.mapper.PaymentMapperHelper;
import com.commercetools.helper.mapper.impl.payment.DefaultPaymentMapperImpl;
import com.commercetools.helper.mapper.impl.payment.InstallmentPaymentMapperImpl;
import com.commercetools.helper.mapper.impl.payment.ProductNameMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.commercetools.payment.constants.ctp.CtpPaymentMethods.DEFAULT;
import static com.commercetools.payment.constants.ctp.CtpPaymentMethods.INSTALLMENT;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
// inject this test case specific classes instead of full application context
@SpringBootTest(classes = {DefaultPaymentMapperImpl.class, InstallmentPaymentMapperImpl.class,
        PaymentMapperHelperImpl.class, PaypalPlusFormatterImpl.class, AddressMapperImpl.class, ProductNameMapper.class})
public class PaymentMapperHelperImplTest {

    @Autowired
    private DefaultPaymentMapperImpl defaultPaymentMapper;

    @Autowired
    private PaymentMapperHelper paymentMapperHelper;

    @Test
    public void getPaymentMapper() throws Exception {
        assertThat(paymentMapperHelper.getPaymentMapper(null)).isEmpty();
        assertThat(paymentMapperHelper.getPaymentMapper("")).isEmpty();
        assertThat(paymentMapperHelper.getPaymentMapper("   ")).isEmpty();
        assertThat(paymentMapperHelper.getPaymentMapper("blah-blah")).isEmpty();
        assertThat(paymentMapperHelper.getPaymentMapper(DEFAULT).orElse(null)).isSameAs(defaultPaymentMapper);
        assertThat(paymentMapperHelper.getPaymentMapper(INSTALLMENT)).isNotEmpty();
    }

    @Test
    public void getPaymentMapperOrDefault() throws Exception {
        assertThat(paymentMapperHelper.getPaymentMapperOrDefault(null)).isSameAs(defaultPaymentMapper);
        assertThat(paymentMapperHelper.getPaymentMapperOrDefault("")).isSameAs(defaultPaymentMapper);
        assertThat(paymentMapperHelper.getPaymentMapperOrDefault("   ")).isSameAs(defaultPaymentMapper);
        assertThat(paymentMapperHelper.getPaymentMapperOrDefault("blah-blah")).isSameAs(defaultPaymentMapper);
        assertThat(paymentMapperHelper.getPaymentMapperOrDefault(DEFAULT)).isSameAs(defaultPaymentMapper);
        assertThat(paymentMapperHelper.getPaymentMapperOrDefault(INSTALLMENT)).isNotSameAs(defaultPaymentMapper);
    }

    @Test
    public void getDefaultPaymentMapper() throws Exception {
        assertThat(paymentMapperHelper.getDefaultPaymentMapper()).isSameAs(defaultPaymentMapper);
    }
}
