package com.itrustmachines.client.verify.service;

import com.itrustmachines.client.config.BnsClientConfig;
import com.itrustmachines.client.login.service.LoginService;
import com.itrustmachines.common.util.KeyInfoUtil;
import com.itrustmachines.common.vo.KeyInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObtainDoneClearanceOrderServiceTest {

    final String bnsServerUrl = "https://bns.itrustmachines.com/";

    @Test
    public void test_obtainDoneClearanceOrder() {
        // given
        final String privateKey = "e0c07f854d30679c32a52af9d3cc72ac13923a4e77ba97337fb2e0cc295ddfde";

        final KeyInfo keyInfo = KeyInfoUtil.buildKeyInfo(privateKey);
        new LoginService(bnsServerUrl, keyInfo, BnsClientConfig.DEFAULT_RETRY_DELAY_SEC).login();
        ObtainDoneClearanceOrderService service = new ObtainDoneClearanceOrderService(bnsServerUrl,
                BnsClientConfig.DEFAULT_RETRY_DELAY_SEC);

        // when
        final long result = service.postDoneClearanceOrder();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void test_obtainDoneClearanceOrder_retryAndFail() {
        // given
        final String bnsServerUrl = "";
        ObtainDoneClearanceOrderService service = new ObtainDoneClearanceOrderService(bnsServerUrl, 0);

        // when, then
        // Assertions.assertThrows(Exception.class, service::obtainDoneClearanceOrder);
        Assertions.assertThrows(Exception.class, service::postDoneClearanceOrder);
    }

}