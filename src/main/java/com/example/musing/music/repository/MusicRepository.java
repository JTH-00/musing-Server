package com.example.musing.music.repository;

import com.example.musing.music.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MusicRepository extends JpaRepository<Music, Long> {
    /*    Optional<Music> findByNameAndArtist(String name, Artist artist);*/
    Optional<Music> findBySongLink(String songLink);
}