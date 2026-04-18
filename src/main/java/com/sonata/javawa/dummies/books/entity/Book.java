package com.sonata.javawa.dummies.books.entity;


import lombok.NonNull;

public record Book(@NonNull String isbn, @NonNull String title) {
}
