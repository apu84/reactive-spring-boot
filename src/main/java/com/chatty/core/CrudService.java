package com.chatty.core;

import com.chatty.core.exception.BadRequestException;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class CrudService<E, R extends ReactiveCrudRepository<E, I>, I> {
    private final R repository;
    public CrudService(R repository) {
        this.repository = repository;
    }
    public Mono<E> save(E entity) {
        return repository.save(entity);
    }
    public Mono<E> get(I id) {
        return repository.findById(id);
    }
    public Flux<E> getAll() {
        return repository.findAll();
    }
    public Mono<E> update(I identifier, Function<E, E> converter) {
        return Mono.just(identifier)
                .flatMap(repository::findById)
                .switchIfEmpty(invalidId())
                .flatMap(entity -> repository.save(converter.apply(entity)));
    }

    public Mono<Void> delete(I identifier) {
        return Mono.just(identifier)
                .flatMap(repository::findById)
                .switchIfEmpty(invalidId())
                .flatMap(repository::delete);
    }

    Mono<E> invalidId() {
        return Mono.error(new BadRequestException("Invalid Id"));
    }
}
