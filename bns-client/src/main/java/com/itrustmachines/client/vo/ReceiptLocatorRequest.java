package com.itrustmachines.client.vo;

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
public class ReceiptLocatorRequest implements Serializable, Cloneable {

    private String address;
    private String indexValueKey;
    private String toSignMessage;
    private SpoSignature sig;

    public ReceiptLocatorRequest clone() {
        return SerializationUtils.clone(this);
    }


    public ReceiptLocatorRequest sign(final @NonNull String privateKey) {
        ReceiptLocatorRequest result;
        try {
            result = clone();
            result.setSig(SignatureUtil.signEthereumMessage(privateKey, result.getToSignMessage()));
        } catch (Exception e) {
            final String errMsg = String.format("sign() error, receiptLocatorRequest=%s", this);
            log.error(errMsg, e);
            throw new RuntimeException(errMsg);
        }
        return result;
    }

}
