package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    TrainRepository trainRepo;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        //first check ticket available or not then check train are going to destination or not
        //if both are valid then book the ticket

        //step 1: convert dto to ticket object
        Ticket ticket = new Ticket();

        int trainId = bookTicketEntryDto.getTrainId();
        Optional<Train> optionalTrain = trainRepository.findById(trainId);

        if(optionalTrain.isPresent()){
            int noOfSeatAvail = optionalTrain.get().getNoOfSeats();
            if(bookTicketEntryDto.getNoOfSeats() <= noOfSeatAvail) {
                //check for the source and destination
                String route = optionalTrain.get().getRoute();
                String[] allStations = route.split(",");

                int sourceIndex = -1;
                int destIndex = -1;

                boolean isSourcePresent = false;
                boolean isDestPresent = false;

                for (int i = 0; i < allStations.length; i++) {
                    String station = allStations[i];
                    if (bookTicketEntryDto.getFromStation().equals(station)) {
                        isSourcePresent = true;
                        sourceIndex = i;
                    }
                    if (bookTicketEntryDto.getToStation().equals(station)) {
                        isDestPresent = true;
                        destIndex = i;
                    }
                }

                boolean allGoodToBook = isSourcePresent && isDestPresent && (sourceIndex != -1 && destIndex != -1) && (sourceIndex < destIndex);

                if (allGoodToBook) {
                    int fare = (destIndex - sourceIndex) * 300;

                    //save the ticket form dto
                    ticket.setFromStation(bookTicketEntryDto.getFromStation());
                    ticket.setToStation(bookTicketEntryDto.getToStation());
                    ticket.setTotalFare(fare);
                    //set the passengerList
                    List<Integer> allPassenger = bookTicketEntryDto.getPassengerIds();
                    List<Passenger> passengerList = ticket.getPassengersList();
                    for (int id : allPassenger) {
                        Optional<Passenger> optionalPassenger = passengerRepository.findById(id);
                        if (optionalPassenger.isPresent()) {
                            passengerList.add(optionalPassenger.get());
                        } else {
                            throw new Exception("Passenger is not registered!");
                        }
                    }
                    noOfSeatAvail -= bookTicketEntryDto.getNoOfSeats();
                    optionalTrain.get().setNoOfSeats(noOfSeatAvail);
                    ticket.setTrain(optionalTrain.get());

                    //set booked ticket in train object
                    optionalTrain.get().getBookedTickets().add(ticket);

                    //save the train to db
                    Train savedTrain = trainRepository.save(optionalTrain.get());

                    //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
                    Optional<Passenger> optionalPassenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());
                    if (optionalPassenger.isPresent()) {
                        optionalPassenger.get().getBookedTickets().add(ticket);
                        Passenger savedPassenger = passengerRepository.save(optionalPassenger.get());
                    } else {
                        throw new Exception("First register yourself");
                    }

                    //now save the ticket to the database
                    Ticket savedTicket = ticketRepository.save(ticket);
                    return savedTicket.getTicketId();
                } else {
                    throw new Exception("Invalid stations");
                }
            } else {
                //check for the source and destination
                String route = optionalTrain.get().getRoute();
                String[] allStations = route.split(",");

                int sourceIndex = -1;
                int destIndex = -1;

                boolean isSourcePresent = false;
                boolean isDestPresent = false;

                for (int i = 0; i < allStations.length; i++) {
                    String station = allStations[i];
                    if (bookTicketEntryDto.getFromStation().equals(station)) {
                        isSourcePresent = true;
                        sourceIndex = i;
                    }
                    if (bookTicketEntryDto.getToStation().equals(station)) {
                        isDestPresent = true;
                        destIndex = i;
                    }
                }

                boolean allGoodToBook = isSourcePresent && isDestPresent && (sourceIndex != -1 && destIndex != -1) && (sourceIndex < destIndex);

                if (!allGoodToBook) {
                    throw new Exception("Invalid stations");
                } else {
                    throw new Exception("Less tickets are available");
                }
            }
        }
        return null;

    }
}
