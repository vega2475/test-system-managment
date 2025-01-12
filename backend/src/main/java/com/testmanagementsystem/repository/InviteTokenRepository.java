package com.testmanagementsystem.repository;

import com.testmanagementsystem.entity.InviteToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InviteTokenRepository extends JpaRepository<InviteToken, Long> {
    Optional<InviteToken> findByToken(String token);
    List<InviteToken> findAllByTestId(Long id);
}
