package com.commercetools.helper.mapper.impl;

import com.commercetools.Application;
import com.commercetools.helper.mapper.PaymentMapperHelper;
import com.commercetools.helper.mapper.impl.payment.DefaultPaymentMapperImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.commercetools.payment.constants.ctp.CtpPaymentMethods.DEFAULT;
import static com.commercetools.payment.constants.ctp.CtpPaymentMethods.INSTALLMENT;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
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
        assertThat(paymentMapperHelper.getPaymentMapper(DEFAULT)).isSameAs(defaultPaymentMapper);
        assertThat(paymentMapperHelper.getPaymentMapper(INSTALLMENT)).isNotNull();
    }

    @Test
    public void getPaymentMapperOrDefault() throws Exception {
        assertThat(paymentMapperHelper.getPaymentMapper(null)).isSameAs(defaultPaymentMapper);
        assertThat(paymentMapperHelper.getPaymentMapper("")).isSameAs(defaultPaymentMapper);
        assertThat(paymentMapperHelper.getPaymentMapper("   ")).isSameAs(defaultPaymentMapper);
        assertThat(paymentMapperHelper.getPaymentMapper("blah-blah")).isSameAs(defaultPaymentMapper);
        assertThat(paymentMapperHelper.getPaymentMapper(DEFAULT)).isSameAs(defaultPaymentMapper);
        assertThat(paymentMapperHelper.getPaymentMapper(INSTALLMENT)).isNotNull();
    }

    @Test
    public void getDefaultPaymentMapper() throws Exception {
        assertThat(paymentMapperHelper.getDefaultPaymentMapper()).isSameAs(defaultPaymentMapper);
    }
}