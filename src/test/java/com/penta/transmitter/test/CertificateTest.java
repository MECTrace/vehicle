package com.penta.transmitter.test;

import com.penta.transmitter.constant.VehicleCertMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CertificateTest {

    @Autowired
    VehicleCertMap vehicleCertMap;

    @Test
    public void signatrue_hex를_binary로_변경하고_인증서의_public_key로_서명검증() throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        // data signatrue String
        String signatureString = "533cf1e6653f2a8e53e4cec34cff91cbedbff4c70c16f61120bea8e50bd9da7277a5f27c9c0e206685ad959073282d26ac2b5a1f70ff1afc0556e71b1c391fbc4c51d0e102965fd97cc1deb9157a309a01431184bfd0b606b2d61ddf42496729f5823f9f0c8b3a720f62bc7879f57015fd3b6538b1aa32ac50fdc9e63c4e312755d87f096c0a5541a274ffd1e1260546f5a43661d1677310ca27e820fff7bf95db4aacb0f53b5bb4795027dd0c8d7597d9304960ea13f100dd7eaf5a1946927fee767700c7d195f8a4675c1ad44e57f363b39b1e98607955678d118957e966955fd8d8d2472a83c8ae62fd891132f509aa206b3fdda27472543bf0c3a66560c8";

        // hex to byte
        byte[] signatureBytes = hexStringToByteArray(signatureString);

        File dataFile = new File("/Users/penta/device_target_file/done/5.2_Chevolet-Bolt-BMS_02구2392_1G1F76E0XJ4114544_2021-08-06T12_00_00.000.csv.gz");

        FileInputStream fis = new FileInputStream("/Users/penta/edgecloud_file/cert/Vehicle/556159dd893bc3e1a25326038ead0f365a0b7ee164807c77ad648f154cc464e0.crt");
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certFactory.generateCertificate(fis);

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(cert.getPublicKey());
        signature.update(Files.readAllBytes(dataFile.toPath()));

        assertEquals(true, signature.verify(signatureBytes));

    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


    /*
    *
    * [Python]
    *
    * from cryptography import x509
from io import BytesIO
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import padding
import os

def load_certificate(path):
    _, ext = os.path.splitext(path)
    with open(path, "rb") as f:
        return x509.load_pem_x509_certificate(f.read())

def test():
    signature_hex = "533cf1e6653f2a8e53e4cec34cff91cbedbff4c70c16f61120bea8e50bd9da7277a5f27c9c0e206685ad959073282d26ac2b5a1f70ff1afc0556e71b1c391fbc4c51d0e102965fd97cc1deb9157a309a01431184bfd0b606b2d61ddf42496729f5823f9f0c8b3a720f62bc7879f57015fd3b6538b1aa32ac50fdc9e63c4e312755d87f096c0a5541a274ffd1e1260546f5a43661d1677310ca27e820fff7bf95db4aacb0f53b5bb4795027dd0c8d7597d9304960ea13f100dd7eaf5a1946927fee767700c7d195f8a4675c1ad44e57f363b39b1e98607955678d118957e966955fd8d8d2472a83c8ae62fd891132f509aa206b3fdda27472543bf0c3a66560c8"
    signature_byte = bytes.fromhex(signature_hex)

    data_file = open("/Users/penta/device_target_file/done/5.2_Chevolet-Bolt-BMS_02구2392_1G1F76E0XJ4114544_2021-08-06T12_00_00.000.csv.gz", "rb")
    cert = load_certificate("/Users/penta/edgecloud_file/cert/Vehicle/556159dd893bc3e1a25326038ead0f365a0b7ee164807c77ad648f154cc464e0.crt")
    public_key = cert.public_key()
    public_key.verify(
        signature_byte,
        data_file.read(),
        padding.PKCS1v15(),
        hashes.SHA256()
    )


test()
    *
    *
    *
    *
    * */


}
