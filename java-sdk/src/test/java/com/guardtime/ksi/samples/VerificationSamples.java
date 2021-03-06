/*
 * Copyright 2013-2016 Guardtime, Inc.
 *
 * This file is part of the Guardtime client SDK.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES, CONDITIONS, OR OTHER LICENSES OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License. "Guardtime"
 * and "KSI" are trademarks or registered trademarks of Guardtime, Inc., and no license to
 * trademarks is granted; Guardtime reserves and retains all trademark rights.
 */
package com.guardtime.ksi.samples;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.guardtime.ksi.KSI;
import com.guardtime.ksi.exceptions.KSIException;
import com.guardtime.ksi.hashing.DataHasher;
import com.guardtime.ksi.publication.PublicationData;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.VerificationContext;
import com.guardtime.ksi.unisignature.verifier.VerificationContextBuilder;
import com.guardtime.ksi.unisignature.verifier.VerificationResult;
import com.guardtime.ksi.unisignature.verifier.policies.CalendarBasedVerificationPolicy;
import com.guardtime.ksi.unisignature.verifier.policies.KeyBasedVerificationPolicy;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;
import com.guardtime.ksi.unisignature.verifier.policies.PublicationsFileBasedVerificationPolicy;
import com.guardtime.ksi.unisignature.verifier.policies.UserProvidedPublicationBasedVerificationPolicy;

public class VerificationSamples extends KsiSamples {

    @Before
    public void setUp() throws KSIException {
        setUpKsi();
    }

    @After
    public void tearDown() {
        tearDownKsi();
    }

    /**
     * Verifies signature against a publication using the publications in the publication file. The
     * signature must be extended for the verification to succeed.
     */
    @Test
    public void verifyExtendedSignatureUsingPublicationsFile() throws IOException, KSIException {
        KSI ksi = getKsi();

        // Read the existing signature, assume it is extended
        KSISignature signature = ksi.read(getFile("signme.txt.extended-ksig"));

        // We need to compute the hash from the original data, to make sure it
        // matches the one in the signature and has not been changed
        // Use the same algorithm as the input hash in the signature
        DataHasher dataHasher = new DataHasher(signature.getInputHash().getAlgorithm());
        dataHasher.addData(getFile("signme.txt"));

        // Do the verification and check the result
        Policy policy = new PublicationsFileBasedVerificationPolicy();
        VerificationResult verificationResult = ksi.verify(signature, policy, dataHasher.getHash());

        if (verificationResult.isOk()) {
            System.out.println("verifyExtendedSignatureUsingPublicationsFile > signature valid");
        } else {
            System.out.println("verifyExtendedSignatureUsingPublicationsFile > verification failed with error code > "
                    + verificationResult.getErrorCode());
        }
    }

    /**
     * Verifies the signature against a publication using the specified publication string (code).
     */
    @Test
    public void verifyExtendedSignatureUsingPublicationsCode() throws IOException, KSIException {
        KSI ksi = getKsi();

        KSISignature signature = ksi.read(getFile("signme.txt.extended-ksig"));

        DataHasher dataHasher = new DataHasher(signature.getInputHash().getAlgorithm());
        dataHasher.addData(getFile("signme.txt"));

        // The trust anchor in this example is the publication code in Financial
        // Times or on Twitter
        String pubString = "AAAAAA-CW45II-AAKWRK-F7FBNM-KB6FNV-DYYFW7-PJQN6F-JKZWBQ-3OQYZO-HCB7RA-YNYAGA-ODRL2V";
        PublicationData publicationData = new PublicationData(pubString);

        // Do the verification and check the result
        Policy policy = new UserProvidedPublicationBasedVerificationPolicy();
        VerificationResult verificationResult = ksi.verify(signature, policy, dataHasher.getHash(), publicationData);

        if (verificationResult.isOk()) {
            System.out.println("verifyExtendedSignatureUsingPublicationsCode > signature valid");
        } else {
            System.out.println(
                    "verifyExtendedSignatureUsingPublicationsCode > signature verification failed with error code > "
                            + verificationResult.getErrorCode());
        }
    }

    /**
     * Verify the given signature against a publication. The signature is not extended but
     * auto-extending is enabled and possible (there is a publication after signing time) so the
     * verification should succeed.
     */
    @Test
    public void verifyExtendedSignatureUsingPublicationsCodeAutoExtend() throws IOException, KSIException {
        KSI ksi = getKsi();

        // Read signature, assume to be not extended
        KSISignature signature = ksi.read(getFile("signme.txt.unextended-ksig"));

        DataHasher dataHasher = new DataHasher(signature.getInputHash().getAlgorithm());
        dataHasher.addData(getFile("signme.txt"));

        String pubString = "AAAAAA-CW45II-AAKWRK-F7FBNM-KB6FNV-DYYFW7-PJQN6F-JKZWBQ-3OQYZO-HCB7RA-YNYAGA-ODRL2V";
        PublicationData publicationData = new PublicationData(pubString);

        // Do the verification and check the result
        Policy policy = new UserProvidedPublicationBasedVerificationPolicy();

        VerificationContext context = new VerificationContextBuilder().setDocumentHash(dataHasher.getHash())
                .setExtendingAllowed(true).setExtenderClient(getSimpleHttpClient()).setSignature(signature)
                .setUserPublication(publicationData).setPublicationsFile(ksi.getPublicationsFile())
                .createVerificationContext();

        VerificationResult verificationResult = ksi.verify(context, policy);

        if (verificationResult.isOk()) {
            System.out.println("verifyExtendedSignatureUsingPublicationsCodeAutoExtend > signature valid");
        } else {
            System.out.println(
                    "verifyExtendedSignatureUsingPublicationsCodeAutoExtend > signature verification failed with error code > "
                            + verificationResult.getErrorCode());
        }
    }

    /**
     * Verifies signature using key-based verification policy.
     */
    @Test
    public void verifyKeyBased() throws IOException, KSIException {
        KSI ksi = getKsi();

        KSISignature signature = ksi.read(getFile("signme.txt.unextended-ksig"));

        DataHasher dataHasher = new DataHasher(signature.getInputHash().getAlgorithm());
        dataHasher.addData(getFile("signme.txt"));

        Policy policy = new KeyBasedVerificationPolicy();
        VerificationResult verificationResult = ksi.verify(signature, policy, dataHasher.getHash());

        if (verificationResult.isOk()) {
            System.out.println("verifyKeyBased > signature valid");
        } else {
            System.out.println("verifyKeyBased > signature verification failed with error code > "
                    + verificationResult.getErrorCode());
        }
    }

    /**
     * Verifies signature using calendar-based verification policy.
     */
    @Test
    public void verifyCalendarBasedUnextended() throws IOException, KSIException {
        KSI ksi = getKsi();

        KSISignature signature = ksi.read(getFile("signme.txt.unextended-ksig"));

        DataHasher dataHasher = new DataHasher(signature.getInputHash().getAlgorithm());
        dataHasher.addData(getFile("signme.txt"));

        Policy policy = new CalendarBasedVerificationPolicy();
        VerificationResult verificationResult = ksi.verify(signature, policy, dataHasher.getHash());

        if (verificationResult.isOk()) {
            System.out.println("verifyCalendarBasedUnextended > signature valid");
        } else {
            System.out.println("verifyCalendarBasedUnextended > signature verification failed with error code > "
                    + verificationResult.getErrorCode());
        }
    }
}
