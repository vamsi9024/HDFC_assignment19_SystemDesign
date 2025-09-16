package com.example.SpringBootSystemDesign.controller;


import com.example.SpringBootSystemDesign.dto.BookingRequest;
import com.example.SpringBootSystemDesign.dto.BookingResponse;
import com.example.SpringBootSystemDesign.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> bookTickets(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.bookTickets(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable Long userId) {
        List<BookingResponse> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(bookings);
    }
}