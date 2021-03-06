/*
 * Copyright (c) 2020 D4L data4life gGmbH / All rights reserved.
 *
 * D4L owns all legal rights, title and interest in and to the Software Development Kit ("SDK"),
 * including any intellectual property rights that subsist in the SDK.
 *
 * The SDK and its documentation may be accessed and used for viewing/review purposes only.
 * Any usage of the SDK for other purposes, including usage for the development of
 * applications/third-party applications shall require the conclusion of a license agreement
 * between you and D4L.
 *
 * If you are interested in licensing the SDK for your own applications/third-party
 * applications and/or if you’d like to contribute to the development of the SDK, please
 * contact D4L by email to help@data4life.care.
 */

package care.data4life.sdk;

import android.content.Context;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import androidx.test.InstrumentationRegistry;
import care.data4life.crypto.GCKey;
import care.data4life.fhir.stu3.model.DocumentReference;
import care.data4life.fhir.stu3.model.DomainResource;
import care.data4life.sdk.config.DataRestrictionException;
import care.data4life.sdk.test.data.model.SymTestData;
import care.data4life.sdk.test.util.AssetsHelper;
import care.data4life.sdk.test.util.DocumentReferenceFactory;
import care.data4life.sdk.util.Base64;
import care.data4life.securestore.SecureStore;
import care.data4life.securestore.SecureStoreCryptor;
import care.data4life.securestore.SecureStoreStorage;

import static com.google.common.truth.Truth.assertThat;

@Ignore("Ignored until deep copy of fhir resource is implemented")
public class FhirServiceInstrumentedTest {

    private static final String PATH = "design-documents/crypto/test-fixture/v1/";
    private static final String DEFAULT_ALIAS = "defaultAlias";

    private FhirService fhirService;
    private GCKey dataKey;

    @Before
    public void setUp() throws Exception {
        Context context = InstrumentationRegistry.getContext();

        SymTestData symTestData = AssetsHelper.loadJson(context, PATH + "symCommonEncrypt.json", SymTestData.class);
        symTestData.getGCKey().getAlgorithm().setIv(Base64.INSTANCE.decode(symTestData.getIv()));
        dataKey = symTestData.getGCKey();

        SecureStore secureStore = new SecureStore(new SecureStoreCryptor(context), new SecureStoreStorage(context));
        CryptoSecureStore storage = new CryptoSecureStore(secureStore);
        CryptoService cryptoService = new CryptoService(DEFAULT_ALIAS, storage);
        fhirService = new FhirService(cryptoService);
    }

    @Test
    public void encryptAndDecryptFhirResource_shouldCompleteWithoutErrors() throws DataRestrictionException.UnsupportedFileType, DataRestrictionException.MaxDataSizeViolation {
        // given
        DocumentReference dummyDocRef = DocumentReferenceFactory.buildDocument();

        // when
        String encryptedResource = fhirService.encryptResource(dataKey, dummyDocRef);
        DomainResource decryptedResource = fhirService.decryptResource(dataKey, DocumentReference.resourceType, encryptedResource);

        assertThat(decryptedResource).isInstanceOf(DocumentReference.class);
        DocumentReference docRef = (DocumentReference) decryptedResource;
        //TODO uncomment when deep copy and equals are implemented
        assertThat(docRef).isEqualTo(dummyDocRef);
    }
}
