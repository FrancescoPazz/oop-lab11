package it.unibo.oop.lab.streams;

import static java.util.stream.Collectors.groupingBy;

import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.RowFilter.Entry;

/**
 *
 */
public final class MusicGroupImpl implements MusicGroup {

    private final Map<String, Integer> albums = new HashMap<>();
    private final Set<Song> songs = new HashSet<>();

    @Override
    public void addAlbum(final String albumName, final int year) {
        this.albums.put(albumName, year);
    }

    @Override
    public void addSong(final String songName, final Optional<String> albumName, final double duration) {
        if (albumName.isPresent() && !this.albums.containsKey(albumName.get())) {
            throw new IllegalArgumentException("invalid album name");
        }
        this.songs.add(new MusicGroupImpl.Song(songName, albumName, duration));
    }

    @Override
    public Stream<String> orderedSongNames() {
        return songs.stream().map(elem -> elem.songName).sorted();
    }

    @Override
    public Stream<String> albumNames() {
        return albums.entrySet().stream()
                                .map(elem -> elem.getKey());
    }

    @Override
    public Stream<String> albumInYear(final int year) {
        return albums.entrySet().stream()
                                .filter(elem -> elem.getValue() == year)
                                .map(elem -> elem.getKey());
    }

    @Override
    public int countSongs(final String albumName) {
        return (int)songs.stream()
                    .filter(e -> e.albumName.isPresent())
                    .filter(e -> e.albumName.get().equals(albumName))
                    .count();
    }

    @Override
    public int countSongsInNoAlbum() {
        return (int)songs.stream()
                         .filter(elem -> elem.albumName.equals(Optional.empty()))
                         .count();
    }

    @Override
    public OptionalDouble averageDurationOfSongs(final String albumName) {
        return OptionalDouble.of(songs.stream()
                    .filter(elem -> elem.albumName.isPresent())
                    .filter(elem -> elem.albumName.get().equals(albumName))
                    .map(elem -> elem.getDuration())
                    .reduce((e1, e2) -> (e1+e2)/2).get());
    }

    @Override
    public Optional<String> longestSong() {
        return songs.stream()
                    .max((e1, e2) -> (int)(e1.getDuration() - e2.getDuration()))
                    .map(e -> e.songName);
    }

    @Override
    public Optional<String> longestAlbum() {
        return Optional.of(songs.stream()
                    .filter(e -> e.getAlbumName().isPresent())
                    .collect(Collectors.groupingBy(
                        e -> e.getAlbumName().get(), 
                        Collectors.summarizingDouble(Song::getDuration)))
                    .entrySet()
                    .stream()
                    .max((e1, e2) -> (int)(e1.getValue().getSum() - e2.getValue().getSum()))
                    .map(e1 -> e1.getKey())
                    .get());

    }

    private static final class Song {

        private final String songName;
        private final Optional<String> albumName;
        private final double duration;
        private int hash;

        Song(final String name, final Optional<String> album, final double len) {
            super();
            this.songName = name;
            this.albumName = album;
            this.duration = len;
        }

        public String getSongName() {
            return songName;
        }

        public Optional<String> getAlbumName() {
            return albumName;
        }

        public double getDuration() {
            return duration;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                hash = songName.hashCode() ^ albumName.hashCode() ^ Double.hashCode(duration);
            }
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Song) {
                final Song other = (Song) obj;
                return albumName.equals(other.albumName) && songName.equals(other.songName)
                        && duration == other.duration;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Song [songName=" + songName + ", albumName=" + albumName + ", duration=" + duration + "]";
        }

    }

}
