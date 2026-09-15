package com.pratham.backend.engine.algorithm;

import com.pratham.backend.entity.FingerprintRecord;

import java.util.*;
import java.util.stream.Collectors;

//TODO: Review (Most important component of this project)
public class Matcher {

    private static final int ZONE_GENERATION_SIZE = 5;
    private static final int MIN_COUPLES_PER_ZONE = 3;
    private static final double MIN_ZONE_COVERAGE = 0.5;
    private static final int OFFSET_BUCKET_MS = 100;

    public record MatchResult(long songId, double score){}

//      findBestMatch(sampleFingerprint, dbMatches):
//          if there are no DB matches:
//              return nothing
//
//          group DB records by song
//          count distinct anchor times in recording
//
//          for every song:
//
//              get all its DB records
//              if fewer than 3 records:
//                  skip song
//
//              group records by anchor time
//              count records at each anchor time
//
//              count anchor times having >= 3 records
//
//              if zero:
//                  skip song
//
//              calculate:
//                  formed zones / recording zones
//              if coverage < 50%:
//                  skip song
//
//              add song to survivors
//
//
//          if no survivors:
//              return nothing
//
//          keep only survivor songs
//          calculate timing scores
//          find song with maximum score
//          return MatchResult(songId, score)
    public static Optional<MatchResult> findBestMatch(
            Map<Integer, Long> sampleFingerprint,
            List<FingerprintRecord> dbMatches) {

        if(dbMatches.isEmpty()) return Optional.empty();

        //group DB rows by songId
        Map<Long, List<FingerprintRecord>> bySong = dbMatches.stream()
                .collect(Collectors.groupingBy(FingerprintRecord::getSongId));

        long recordingZoneCount = sampleFingerprint.values().stream().distinct().count();
        if(recordingZoneCount == 0) return Optional.empty();

        Set<Long> survivingSongIds = new HashSet<>();

        for(var entry: bySong.entrySet()){
            long songId = entry.getKey();
            List<FingerprintRecord> couples = entry.getValue();

            //reject - can't form even 1 zone with few total couples
            if(couples.size() < MIN_COUPLES_PER_ZONE) continue;

            //group by DB's own anchor time
            Map<Long, Long> byAnchorTime = couples.stream()
                    .collect(Collectors.groupingBy(
                            FingerprintRecord::getAnchorTimeMs, Collectors.counting()
                    ));

            long formedZones = byAnchorTime.values().stream()
                    .filter(count -> count >= MIN_COUPLES_PER_ZONE)
                    .count();

            if(formedZones == 0)    continue;;

            //keep only songs that form at least 50% of target zones present in recording
            double coverage = (double) formedZones / recordingZoneCount;
            if(coverage < MIN_ZONE_COVERAGE)    continue;

            survivingSongIds.add(songId);
        }

        if(survivingSongIds.isEmpty())  return Optional.empty();

        //check time coherency and pick final survivors
        Map<Long, List<FingerprintRecord>> survivors = bySong.entrySet().stream()
                .filter(e -> survivingSongIds.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return analyzeRelativeTiming(sampleFingerprint, survivors).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> new MatchResult(e.getKey(), e.getValue()));
    }


// analyzeRelativeTiming():
//
//     for every surviving song:
//         create offset counter
//
//         for every DB record:
//             get recording time for its address
//             calculate:
//                 DB anchor time - recording time
//             convert offset to 100ms bucket
//             increment bucket count
//
//         take largest bucket count
//         store it as song's score
//
//     return all song scores
    private static Map<Long, Double> analyzeRelativeTiming(
            Map<Integer, Long> sampleFingerprint,
            Map<Long, List<FingerprintRecord>> bySong
    ){

        Map<Long, Double> scores = new HashMap<>();

        for(var entry : bySong.entrySet()){
            Map<Long, Integer> offsetCounts = new HashMap<>();

            for (FingerprintRecord record : entry.getValue()) {
                Long sampleTime = sampleFingerprint.get(record.getAddress());
                if (sampleTime == null) continue;

                long offset = record.getAnchorTimeMs() - sampleTime;
                long bucket = offset / OFFSET_BUCKET_MS;
                offsetCounts.merge(bucket, 1, Integer::sum);
            }

            int maxCount = offsetCounts.values().stream()
                    .mapToInt(Integer::intValue).max().orElse(0);
            scores.put(entry.getKey(), (double) maxCount);
        }

        return scores;
    }

}
