package com.appskimo.ktube.service.impl;

import java.util.Calendar;
import java.util.Locale;

import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.appskimo.ktube.domain.persist.YouTubeRepository;
import com.appskimo.ktube.service.VideoService;

@Component
@Transactional(readOnly = true)
public class VideoServiceImpl implements VideoService {
    @Autowired private YouTubeRepository youTubeRepository;

    private static final FastDateFormat fdf = FastDateFormat.getInstance("yyyyMMdd");
    private static final FastDateFormat fdf2 = FastDateFormat.getInstance("yyyyMM");

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void updateRankDaily() {
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        int dateIdx = Integer.parseInt(fdf.format(calendar));

        youTubeRepository.deleteVideoRankDaily(dateIdx);
        youTubeRepository.insertVideoRankDaily(dateIdx);

        calendar.add(Calendar.DAY_OF_MONTH, -2);
        dateIdx = Integer.parseInt(fdf.format(calendar));
        youTubeRepository.deleteVideoViewCount(dateIdx);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void updateRankWeekly() {
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        int fromIdx = Integer.parseInt(fdf.format(calendar.getTime()));
        calendar.add(Calendar.DAY_OF_MONTH, +6);
        int toIdx = Integer.parseInt(fdf.format(calendar.getTime()));

        youTubeRepository.deleteVideoRankWeekly(fromIdx);
        youTubeRepository.insertVideoRankWeekly(fromIdx, toIdx);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void updateRankMonthly() {
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.add(Calendar.MONTH, -1);
        int monthIdx = Integer.parseInt(fdf2.format(calendar.getTime()));

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int fromIdx = Integer.parseInt(fdf.format(calendar.getTime()));

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        int toIdx = Integer.parseInt(fdf.format(calendar.getTime()));

        youTubeRepository.deleteVideoRankMonthly(monthIdx);
        youTubeRepository.insertVideoRankMonthly(fromIdx, toIdx, monthIdx);
    }



    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void updateFancamRankDaily() {
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        int dateIdx = Integer.parseInt(fdf.format(calendar));

        youTubeRepository.deleteFancamVideoRankDaily(dateIdx);
        youTubeRepository.insertFancamVideoRankDaily(dateIdx);

        calendar.add(Calendar.DAY_OF_MONTH, -2);
        dateIdx = Integer.parseInt(fdf.format(calendar));
        youTubeRepository.deleteFancamVideoViewCount(dateIdx);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void updateFancamRankWeekly() {
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        int fromIdx = Integer.parseInt(fdf.format(calendar.getTime()));
        calendar.add(Calendar.DAY_OF_MONTH, +6);
        int toIdx = Integer.parseInt(fdf.format(calendar.getTime()));

        youTubeRepository.deleteFancamVideoRankWeekly(fromIdx);
        youTubeRepository.insertFancamVideoRankWeekly(fromIdx, toIdx);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void updateFancamRankMonthly() {
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.add(Calendar.MONTH, -1);
        int monthIdx = Integer.parseInt(fdf2.format(calendar.getTime()));

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int fromIdx = Integer.parseInt(fdf.format(calendar.getTime()));

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        int toIdx = Integer.parseInt(fdf.format(calendar.getTime()));

        youTubeRepository.deleteFancamVideoRankMonthly(monthIdx);
        youTubeRepository.insertFancamVideoRankMonthly(fromIdx, toIdx, monthIdx);
    }



    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void updateKaraokeRankDaily() {
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        int dateIdx = Integer.parseInt(fdf.format(calendar));

        youTubeRepository.deleteKaraokeVideoRankDaily(dateIdx);
        youTubeRepository.insertKaraokeVideoRankDaily(dateIdx);

        calendar.add(Calendar.DAY_OF_MONTH, -2);
        dateIdx = Integer.parseInt(fdf.format(calendar));
        youTubeRepository.deleteKaraokeVideoViewCount(dateIdx);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void updateKaraokeRankWeekly() {
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        int fromIdx = Integer.parseInt(fdf.format(calendar.getTime()));
        calendar.add(Calendar.DAY_OF_MONTH, +6);
        int toIdx = Integer.parseInt(fdf.format(calendar.getTime()));

        youTubeRepository.deleteKaraokeVideoRankWeekly(fromIdx);
        youTubeRepository.insertKaraokeVideoRankWeekly(fromIdx, toIdx);
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void updateKaraokeRankMonthly() {
        Calendar calendar = Calendar.getInstance(Locale.KOREA);
        calendar.add(Calendar.MONTH, -1);
        int monthIdx = Integer.parseInt(fdf2.format(calendar.getTime()));

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int fromIdx = Integer.parseInt(fdf.format(calendar.getTime()));

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        int toIdx = Integer.parseInt(fdf.format(calendar.getTime()));

        youTubeRepository.deleteKaraokeVideoRankMonthly(monthIdx);
        youTubeRepository.insertKaraokeVideoRankMonthly(fromIdx, toIdx, monthIdx);
    }

}
