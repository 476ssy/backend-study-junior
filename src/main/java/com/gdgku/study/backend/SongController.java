package com.gdgku.study.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import java.net.URI;

@RestController
@RequestMapping("/songs")   // 경로가 합쳐져서 계산됨. 앞으로 메서드에 쓰는 경로는 /songs 뒤에 붙는 부분만 적으면 됨.
public class SongController {

  private final List<Song> songList = new ArrayList<>();
  private long nextId = 1L;

  public static class Song {
    private Long id;
    private String title;
    private String artist;
    private String genre;

    public Song() {}
    public Song(Long id, String title, String artist, String genre) {
      this.id = id;
      this.title = title;
      this.artist = artist;
      this.genre = genre;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre;} 

  }

  @PostMapping
  public ResponseEntity<Song> createSong(@RequestBody Song song) {  //ResponseEntity를 쓰면 200이 아닌 코드를 전달 가능
      song.setId(nextId++);
      songList.add(song);
      return ResponseEntity.created(URI.create("/songs/" + song.getId())).body(song);
  }

  @GetMapping
  public List<Song> getSongs(@RequestParam(required = false) String artist) {
    if (artist == null) {  
      return songList; 
    }
    List<Song> result = new ArrayList<>();
    for (Song s : songList) {
      if (s.getArtist().equals(artist)) {
        result.add(s);
      }
    }
    return result;
  }

  @GetMapping("/{id}")
  public ResponseEntity<Song> getSongById(@PathVariable Long id) {
    for (Song s : songList) {
      if (s.getId().equals(id)) {
        return ResponseEntity.ok(s);
      }
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSong(@PathVariable Long id) {
    boolean removed = songList.removeIf(s -> s.getId().equals(id));
    if (removed) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }
}
