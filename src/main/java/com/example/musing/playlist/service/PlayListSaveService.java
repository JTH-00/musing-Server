package com.example.musing.playlist.service;


import com.example.musing.music.entity.Music;
import com.example.musing.music.repository.MusicRepository;
import com.example.musing.playlist.dto.PlaylistListResponse;
import com.example.musing.playlist.dto.PlaylistRepresentativeDto;
import com.example.musing.playlist.dto.PlaylistResponse;
import com.example.musing.playlist.entity.PlayList;
import com.example.musing.playlist.repository.PlayListRepository;
import com.example.musing.playlist_music.entity.PlaylistMusic;
import com.example.musing.playlist_music.repository.PlayListMusicRepository;
import com.example.musing.user.entity.User;
import com.example.musing.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PlayListSaveService {

    private final PlayListRepository playListRepository;
    private final UserRepository userRepository;
    private final MusicRepository musicRepository;
    private final PlayListMusicRepository playlistMusicRepository;

    // 생성자에서 스프링 빈 주입
    public PlayListSaveService(PlayListRepository playListRepository,
                               UserRepository userRepository,
                               MusicRepository musicRepository,
                               PlayListMusicRepository playlistMusicRepository) {
        this.playListRepository = playListRepository;
        this.userRepository = userRepository;
        this.musicRepository = musicRepository;
        this.playlistMusicRepository = playlistMusicRepository;
    }

    @Transactional
    public void savePlayList(PlaylistResponse dto) {
        User user = getCurrentUser();

        long userPlaylistCount = playListRepository.countByUser(user);
        if (userPlaylistCount > 3) {
            throw new IllegalArgumentException("사용자는 최대 3개의 플레이리스트만 가질 수 있습니다. 보유 플레이리스트를 정리해주세요");
        }
        if (playListRepository.existsByYoutubePlaylistIdAndUserId(dto.getRepresentative().getYoutubePlaylistId(),user.getId())) {

            throw new IllegalArgumentException("이미 등록된 플레이리스트 id입니다.");
        }

        PlaylistRepresentativeDto representative = dto.getRepresentative();

        PlayList playList = PlayList.builder()
                .listname(representative.getListName())
                .itemCount((long) dto.getVideoList().size())
                .youtubePlaylistId(representative.getYoutubePlaylistId())
                .youtubeLink(representative.getYoutubePlaylistUrl())
                .description(representative.getDescription())
                .thumbnail(representative.getThumbnailUrl())
                .user(user)
                .build();

        playListRepository.save(playList);

        for (PlaylistListResponse video : dto.getVideoList()) {
            Music music = musicRepository.findBySongLink(video.getSongLink())
                    .orElseGet(() -> musicRepository.save(
                            Music.builder()
                                    .name(video.getName())
                                    .albumName("N/A")
                                    .songLink(video.getSongLink())
                                    .thumbNailLink(video.getThumbNailLink())
                                    .build()
                    ));

            PlaylistMusic playlistMusic = PlaylistMusic.builder()
                    .playList(playList)
                    .music(music)
                    .build();

            playlistMusicRepository.save(playlistMusic);
        }
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String userId = auth.getName();
        return userRepository.findById(userId).orElse(null);
    }
}
