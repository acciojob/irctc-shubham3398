package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        Train train = new Train();
        List<Station> allStation = trainEntryDto.getStationRoute();

        StringBuilder route = new StringBuilder();
        for(Station station : allStation){
            route.append(station+",");
        }

        train.setRoute(route.toString());

        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setBookedTickets(new ArrayList<>());

        //save this train to dataBase
        Train savedTrain = trainRepository.save(train);

        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Optional<Train> optionalTrain = trainRepository.findById(seatAvailabilityEntryDto.getTrainId());
        if(optionalTrain.isPresent()){
            Train train = optionalTrain.get();

            //get the list of booked ticket
            List<Ticket> bookedTicket = train.getBookedTickets();
            int[] passCount = new int[train.getRoute().split(",").length];

            int srcIndex = 0;
            int destIndex = 0;

            int seatAvail = train.getNoOfSeats();
            int totalSeatAvail = seatAvail;

            for(int ind = 0; ind < bookedTicket.size(); ind++){
                Ticket ticket = bookedTicket.get(ind);

                Station from = ticket.getFromStation();
                Station to = ticket.getToStation();

                String[] stations = train.getRoute().split(",");
                for (int i = 0; i < stations.length; i++){
                    String station = stations[i];
                    if(station.equals(from)) {
                        passCount[i] += ticket.getPassengersList().size();
                    }
                    if (station.equals(to)){
                        passCount[i] -= ticket.getPassengersList().size();
                    }
                    if(station.equals(seatAvailabilityEntryDto.getFromStation())){
                        srcIndex = i;
                    }
                    if(station.equals(seatAvailabilityEntryDto.getToStation())){
                        destIndex = i;
                    }
                }
            }
            //find the empty seat between two station
            int max = -1;
            int currSum = 0;
            for(int i = 0; i < passCount.length; i++){
                currSum += passCount[i];
                passCount[i] = currSum;
                max = Math.max(max, passCount[i]);
            }
            int maxSeatBookedBetween2station= -1;
            for(int i = srcIndex; i <= destIndex; i++){
                maxSeatBookedBetween2station = Math.max(maxSeatBookedBetween2station, passCount[i]);
            }

            int remainingSeatInTrain = max - maxSeatBookedBetween2station;

            totalSeatAvail += remainingSeatInTrain;
            return totalSeatAvail;
        }

       return null;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //in a happy case we need to find out the number of such people.

        Optional<Train> optionalTrain = trainRepository.findById(trainId);
        if(optionalTrain.isPresent()){
            Train train = optionalTrain.get();

            String[] stations = train.getRoute().split(",");
            boolean stationPresent = false;
            for(String stn : stations){
                if(stn.equals(station)) stationPresent = true;
            }

            if(!stationPresent) throw new Exception("Train is not passing from this station");

            //count the no of passenger
            List<Ticket> bookedTicket = train.getBookedTickets();
            int noOfPassenger = 0;
            for(Ticket ticket : bookedTicket){
                if(ticket.getToStation().equals(station)){
                    noOfPassenger += ticket.getPassengersList().size();
                }
            }
            return noOfPassenger;
        }

        return 0;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Optional<Train> optionalTrain = trainRepository.findById(trainId);
        Train train = optionalTrain.get();

        List<Ticket> bookedTicket = train.getBookedTickets();
        int oldestAge = 0;
        for(Ticket ticket : bookedTicket){
            List<Passenger> passengerList = ticket.getPassengersList();
            for(Passenger pass : passengerList){
                oldestAge = Math.max(oldestAge, pass.getAge());
            }
        }

        return oldestAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        //first get the list of train who passed thorught this station
        List<Train> allTrain = trainRepository.findAll();

        List<Integer> passThroughStation = new ArrayList<>();
        int trainCount = 0;
        for(Train train : allTrain){
            String[] stations = train.getRoute().split(",");
            for(String st : stations){
                if(st.equals(station)){
                    if(train.getDepartureTime().compareTo(startTime) >= 0 && train.getDepartureTime().compareTo(endTime) <= 0){
                        trainCount++;
                    }
                }
            }
        }
        passThroughStation.add(trainCount);
        if(!passThroughStation.isEmpty()) return passThroughStation;
        return null;
    }

}
