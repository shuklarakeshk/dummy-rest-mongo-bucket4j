package com.sonata.javawa.dummies.books.boundary;


import com.sonata.javawa.dummies.books.entity.Book;
import com.sonata.javawa.dummies.books.entity.BookRepository;
import com.sonata.javawa.dummies.ratelimit.boundary.RateLimiter;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;

import java.util.List;

@Path("/")
@AllArgsConstructor
public class BookRestResource {
    private BookRepository bookRepository;
    private RateLimiter rateLimiter;
    @GET
    @Path("/books/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> findAll(@PathParam("id")  String id) {
       return rateLimiter.isRateLimited(id).chain(this::getResponse);
    }
    private Uni<Response> getResponse(boolean isRateLimited)
    {
        Uni<Response> toReturn;
        if( isRateLimited)
        {
            toReturn = Uni.createFrom().item(Response.status(429).build());
        }
        else
        {
            toReturn = bookRepository.findAll().list().map( e -> Response.ok(e).build());
        }
        return toReturn;
    }
    @POST
    @Path("/books")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Book> addOneBook(Book book) {
        return bookRepository.persist(book);
    }
}
