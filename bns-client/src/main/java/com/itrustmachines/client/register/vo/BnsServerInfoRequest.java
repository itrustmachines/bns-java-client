package com.itrustmachines.client.register.vo;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationUtils;

import com.itrustmachines.common.util.SignatureUtil;
import com.itrustmachines.common.vo.SpoSignature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class BnsServerInfoRequest implements Serializable, Cloneable {

    private String address;
    private String toSignMessage;
    private SpoSignature sig;

    public BnsServerInfoRequest clone() {
        return SerializationUtils.clone(this);
    }


    public BnsServerInfoRequest sign(final @NonNull String privateKey) {

        BnsServerInfoRequest result;
        try {
            result = clone();
            result.setSig(SignatureUtil.signEthereumMessage(privateKey, result.getToSignMessage()));
        } catch (Exception e) {
            final String errMsg = String.format("sign() error, bnsServerInfoRequest=%s", this);
            log.error(errMsg, e);
            throw new RuntimeException(errMsg);
        }
        return result;
    }

}
