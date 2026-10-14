package com.bogdusik.chat.repository;

import java.util.List;

import com.bogdusik.chat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /** The newest messages first, with their senders loaded in the same query. */
    @Query("select m from Message m left join fetch m.user order by m.sentAt desc, m.id desc")
    List<Message> findLatest(Pageable pageable);
}
