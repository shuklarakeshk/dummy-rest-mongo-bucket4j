package com.sonata.javawa.dummies.books.entity;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BookRepository implements ReactivePanacheMongoRepository<Book> {
}
