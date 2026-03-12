package com.btgpactual.fund.domain.repository;

import com.btgpactual.fund.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    User save(User user);
}
