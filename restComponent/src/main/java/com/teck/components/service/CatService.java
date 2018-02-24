package com.teck.components.service;

import com.teck.components.domain.Cat;

import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.stereotype.Service;

/*
 * Sample service to demonstrate what the API would use to get things done
 */
@Service
public class CatService {

    private static final Logger log = LoggerFactory.getLogger(CatService.class);

    //@Autowired
    //private CatRepository catRepository;

    @Autowired
    CounterService counterService;

    @Autowired
    GaugeService gaugeService;

    public CatService() {
    }

    public Cat createCat(Cat cat) {
        //catRepository.save(cat);
        return cat;
    }

    public Cat getCat(long id) {
        //catRepository.findOne(id);
        return new Cat("name", 4);
    }

    public void updateCat(Cat cat) {
        //catRepository.save(cat);
    }

    /*
    public void deleteHotel(Long id) {
        hotelRepository.delete(id);
    }

    //http://goo.gl/7fxvVf
    public Page<Hotel> getAllHotels(Integer page, Integer size) {
        Page pageOfHotels = hotelRepository.findAll(new PageRequest(page, size));
        // example of adding to the /metrics
        if (size > 50) {
            counterService.increment("Khoubyari.HotelService.getAll.largePayload");
        }
        return pageOfHotels;
    }
    */
}
