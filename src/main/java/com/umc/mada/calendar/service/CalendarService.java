package com.umc.mada.calendar.service;

import com.umc.mada.calendar.domain.Calendar;
import com.umc.mada.calendar.domain.ManyD_dayException;
import com.umc.mada.calendar.domain.SameCalendarNameExist;
import com.umc.mada.calendar.dto.CalendarRequestDto;
import com.umc.mada.calendar.dto.CalendarResponseDto;
import com.umc.mada.calendar.repository.CalendarRepository;
import com.umc.mada.user.domain.User;
import com.umc.mada.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j //로그처리
@Transactional //트렌젝션 처리
public class CalendarService {
    private final CalendarRepository calendarRepository;
    private final UserRepository userRepository;
    @Autowired
    public  CalendarService(CalendarRepository calendarRepository, UserRepository userRepository){
        this.calendarRepository = calendarRepository;
        this.userRepository = userRepository;

    }
    public List<CalendarResponseDto> readDday(Authentication authentication){
        User user = this.getUser(authentication);
        List<Calendar> calendarList = calendarRepository.findAllByUserAndDday(user,'N');
        List<CalendarResponseDto> calendarResponseDtoList = new ArrayList<>();
        for (Calendar calendar: calendarList) {
            calendarResponseDtoList.add(this.calendarToDto(calendar));
        }
        return calendarResponseDtoList;
    }

    public List<CalendarResponseDto> readMonthCalendar(Authentication authentication, int month){
        User user = this.getUser(authentication);
        List<Calendar> calendarList = readCalendarsByMonth(calendarRepository.findAllByUser(user),month);
        List<CalendarResponseDto> calendarResponseDtoList = new ArrayList<>();
        for (Calendar calendar: calendarList) {
            calendarResponseDtoList.add(this.calendarToDto(calendar));
        }
        return calendarResponseDtoList;
    }
    //동일 이름의 일정이 동일한 날짜에 있는지 검증
    //캘린더 생성코드
    public CalendarResponseDto calendarCreate(Authentication authentication, CalendarRequestDto calendarRequestDto) throws ManyD_dayException,SameCalendarNameExist{
        User user = this.getUser(authentication);
        if (calendarRequestDto.getDday() == 'Y' && tooManyD_dayExists(authentication)) {
            throw new ManyD_dayException("D_day가 3개 이상 존재합니다");
        }
        if(calendarNameExist(authentication,calendarRequestDto)){
            throw new SameCalendarNameExist("기간 중에 동일한 이름의 캘린더가 존재합니다");
        }
        Calendar calendar = Calendar.builder()
                //User Entity 부재
                .user(user)
                .calenderName(calendarRequestDto.getCalenderName())
                .dday(calendarRequestDto.getDday())
                .repeat(calendarRequestDto.getRepeat())
                .memo(calendarRequestDto.getMemo())
                .startDate(calendarRequestDto.getStartDate())
                .endDate(calendarRequestDto.getEndDate())
                .build();
        calendarRepository.save(calendar);
        return new CalendarResponseDto(calendarRequestDto.getCalenderName(),calendarRequestDto.getStartDate(),calendarRequestDto.getEndDate(),calendarRequestDto.getDday(), calendarRequestDto.getRepeat(),calendarRequestDto.getMemo(), calendarRequestDto.getColor());
    }
    public CalendarResponseDto calendarEdit(Authentication authentication, Long id, CalendarRequestDto calendarRequestDto){
        User user = this.getUser(authentication);

        Calendar calendar = calendarRepository.findCalendarById(id);
        calendar.setMemo(calendarRequestDto.getMemo());
        calendar.setStartDate(calendarRequestDto.getStartDate());
        calendar.setEndDate(calendarRequestDto.getEndDate());
        calendar.setCalenderName(calendarRequestDto.getCalenderName());
        calendar.setColor(calendarRequestDto.getColor());
        calendarRepository.save(calendar);
        return new CalendarResponseDto(calendarRequestDto.getCalenderName(),calendarRequestDto.getStartDate(),calendarRequestDto.getEndDate(),calendarRequestDto.getDday(),calendarRequestDto.getRepeat(),calendarRequestDto.getMemo(), calendarRequestDto.getColor());
    }
    public List<CalendarResponseDto> calendarsReadByDate(Authentication authentication,Date date){
        User user = this.getUser(authentication);

        List<Calendar> calendarList = readCalendarsByDate(calendarRepository.findAllByUser(user),date);
        List<CalendarResponseDto> calendarResponseDtoList = new ArrayList<>();
        for (Calendar calendar: calendarList) {
            calendarResponseDtoList.add(this.calendarToDto(calendar));
        }
        return calendarResponseDtoList;
    }
    public List<CalendarResponseDto> calendarsRead(Authentication authentication) {
        User user = this.getUser(authentication);
        List<Calendar> calendarList = calendarRepository.findAllByUser(user);
        List<CalendarResponseDto> calendarResponseDtoList = new ArrayList<>();
        for (Calendar calendar: calendarList) {
            calendarResponseDtoList.add(this.calendarToDto(calendar));
        }
        return calendarResponseDtoList;
    }

    public CalendarResponseDto calendarDelete(Authentication authentication, Long id) throws NoSuchElementException{
        User user = this.getUser(authentication);
        Calendar calendar = calendarRepository.findCalendarByUserAndId(user,id);
        calendarRepository.deleteCalendarById(id);
        return this.calendarToDto(calendar);
    }

    public boolean tooManyD_dayExists(Authentication authentication){
        User user = this.getUser(authentication);
        return calendarRepository.findAllByUser(user).size() >= 3;
    }

    public boolean calendarNameExist(Authentication authentication ,CalendarRequestDto calendarRequestDto) {
        User user = this.getUser(authentication);
        return calendarRepository.existsCalendarByUserAndEndDateBetweenAndCalenderName(
                user
                , calendarRequestDto.getStartDate()
                , calendarRequestDto.getEndDate()
                , calendarRequestDto.getCalenderName()
        );
    }

    public List<Calendar> readCalendarsByDate(List<Calendar> calendarList, Date date){
        return calendarList.stream()
                .filter(calendar -> calendar.getStartDate().compareTo(date)<=0 &&calendar.getEndDate().compareTo(date)>=0)
                .collect(Collectors.toList());
    }

    private List<Calendar> readCalendarsByMonth(List<Calendar> calendarList, int month){
        return calendarList.stream()
                .filter(calendar -> calendar.getStartDate().toLocalDate().getMonthValue()<=month&&calendar.getEndDate().toLocalDate().getMonthValue()>=month)
                .collect(Collectors.toList());
    }
    private User getUser(Authentication authentication) throws NoSuchElementException {
        try{
            Optional<User> userOptional = userRepository.findByAuthId(authentication.getName());
            return userOptional.get();
        }catch (RuntimeException e){
            throw new NoSuchElementException();
        }
    }
    private CalendarResponseDto calendarToDto(Calendar calendar){
        return CalendarResponseDto.builder()
                .calenderName(calendar.getCalenderName())
                .startDate(calendar.getStartDate())
                .endDate(calendar.getEndDate())
                .color(calendar.getColor())
                .dday(calendar.getDday())
                .memo(calendar.getMemo())
                .repeat(calendar.getRepeat())
                .build();
    }

}
