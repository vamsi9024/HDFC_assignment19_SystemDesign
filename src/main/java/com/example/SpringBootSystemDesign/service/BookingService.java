package com.example.SpringBootSystemDesign.service;


import com.example.SpringBootSystemDesign.dto.BookingRequest;
import com.example.SpringBootSystemDesign.dto.BookingResponse;
import com.example.SpringBootSystemDesign.entity.Booking;
import com.example.SpringBootSystemDesign.entity.Seat;
import com.example.SpringBootSystemDesign.entity.Show;
import com.example.SpringBootSystemDesign.entity.User;
import com.example.SpringBootSystemDesign.exception.ResourceNotFoundException;
import com.example.SpringBootSystemDesign.exception.SeatNotAvailableException;
import com.example.SpringBootSystemDesign.repository.BookingRepository;
import com.example.SpringBootSystemDesign.repository.SeatRepository;
import com.example.SpringBootSystemDesign.repository.ShowRepository;
import com.example.SpringBootSystemDesign.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private PaymentService paymentService;

    @Transactional
    public BookingResponse bookTickets(BookingRequest request) {
        // Fetch user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        // Fetch show
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + request.getShowId()));

        // Fetch and validate seats
        List<Seat> requestedSeats = seatRepository.findByShowIdAndSeatNumbers(
                request.getShowId(), request.getSeatNumbers());

        if (requestedSeats.size() != request.getSeatNumbers().size()) {
            throw new ResourceNotFoundException("Some seats not found");
        }

        // Check if all seats are available
        List<Seat> unavailableSeats = requestedSeats.stream()
                .filter(seat -> seat.getStatus() != Seat.SeatStatus.AVAILABLE)
                .collect(Collectors.toList());

        if (!unavailableSeats.isEmpty()) {
            List<String> unavailableSeatNumbers = unavailableSeats.stream()
                    .map(Seat::getSeatNumber)
                    .collect(Collectors.toList());
            throw new SeatNotAvailableException("Seats not available: " + unavailableSeatNumbers);
        }

        // Calculate total amount
        double totalAmount = requestedSeats.stream()
                .mapToDouble(Seat::getPrice)
                .sum();

        // Create booking
        Booking booking = new Booking(user, show, requestedSeats, totalAmount);

        // Update seat status to BOOKED
        List<Long> seatIds = requestedSeats.stream()
                .map(Seat::getSeatId)
                .collect(Collectors.toList());

        seatRepository.updateSeatStatus(seatIds, Seat.SeatStatus.BOOKED);

        // Process payment (mocked)
        boolean paymentSuccess = paymentService.processPayment(booking.getBookingId(), totalAmount);

        if (paymentSuccess) {
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
        } else {
            // Revert seat status if payment fails
            seatRepository.updateSeatStatus(seatIds, Seat.SeatStatus.AVAILABLE);
            throw new RuntimeException("Payment failed");
        }

        // Save booking
        booking = bookingRepository.save(booking);

        // Create response
        return new BookingResponse(
                booking.getBookingId(),
                show.getMovie().getTitle(),
                show.getTheater().getName(),
                show.getShowTime(),
                request.getSeatNumbers(),
                totalAmount,
                booking.getStatus()
        );
    }

    public List<BookingResponse> getUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByBookingTimeDesc(userId);

        return bookings.stream()
                .map(booking -> new BookingResponse(
                        booking.getBookingId(),
                        booking.getShow().getMovie().getTitle(),
                        booking.getShow().getTheater().getName(),
                        booking.getShow().getShowTime(),
                        booking.getSeats().stream().map(Seat::getSeatNumber).collect(Collectors.toList()),
                        booking.getTotalAmount(),
                        booking.getStatus()
                ))
                .collect(Collectors.toList());
    }
}
