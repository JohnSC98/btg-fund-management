package com.btgpactual.fund.adapter.persistence;

import com.btgpactual.fund.adapter.persistence.mongo.MongoUserRepository;
import com.btgpactual.fund.domain.model.User;
import com.btgpactual.fund.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final MongoUserRepository mongoRepository;

    @Override
    public Optional<User> findById(String id) {
        return mongoRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return mongoRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return mongoRepository.save(user);
    }
}
