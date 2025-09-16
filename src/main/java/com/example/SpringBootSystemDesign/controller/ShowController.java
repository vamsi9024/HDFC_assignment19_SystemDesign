package com.example.SpringBootSystemDesign.controller;

import com.example.SpringBootSystemDesign.dto.SeatResponse;
import com.example.SpringBootSystemDesign.entity.Seat;
import com.example.SpringBootSystemDesign.entity.Show;
import com.example.SpringBootSystemDesign.repository.SeatRepository;
import com.example.SpringBootSystemDesign.repository.ShowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/shows")
@CrossOrigin(origins = "*")
public class ShowController {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeatRepository seatRepository;

    @GetMapping
    public ResponseEntity<List<Show>> getShows(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long theaterId) {

        List<Show> shows;

        if (movieId != null && theaterId != null) {
            shows = showRepository.findByMovieIdAndTheaterId(movieId, theaterId);
        } else if (movieId != null) {
            shows = showRepository.findByMovieId(movieId);
        } else if (theaterId != null) {
            shows = showRepository.findByTheaterId(theaterId);
        } else {
            shows = showRepository.findAll();
        }

        return ResponseEntity.ok(shows);
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatResponse>> getShowSeats(@PathVariable Long id) {
        List<Seat> seats = seatRepository.findByShowId(id);

        List<SeatResponse> seatResponses = seats.stream()
                .map(seat -> new SeatResponse(
                        seat.getSeatId(),
                        seat.getSeatNumber(),
                        seat.getStatus(),
                        seat.getPrice()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(seatResponses);
    }
}
