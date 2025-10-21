package com.pki.pki_backend.repository;

import com.pki.pki_backend.model.CertificateRequest;
import com.pki.pki_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateRequestRepository extends JpaRepository<CertificateRequest, Long> {

    List<CertificateRequest> findByRequester(User requester);

    List<CertificateRequest> findByStatus(CertificateRequest.RequestStatus status);

    List<CertificateRequest> findByRequesterOrderByRequestDateDesc(User requester);

    List<CertificateRequest> findAllByOrderByRequestDateDesc();
}
