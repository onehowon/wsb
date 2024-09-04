package com.ebiz.wsb.domain.notice.application;

import com.ebiz.wsb.domain.notice.entity.NoticeType;
import com.ebiz.wsb.domain.notice.repository.NoticeTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeTypeService {

    @Autowired
    private NoticeTypeRepository noticeTypeRepository;

    public List<NoticeType> getAllNoticeTypes(){
        return noticeTypeRepository.findAll();
    }

    public NoticeType getNoticeTypeById(Long id){
        return noticeTypeRepository.findById(id).orElse(null);
    }

    public NoticeType saveNoticeType(NoticeType noticeType){
        return noticeTypeRepository.save(noticeType);
    }

    public void deleteNoticeType(Long id){
        noticeTypeRepository.deleteById(id);
    }

}
