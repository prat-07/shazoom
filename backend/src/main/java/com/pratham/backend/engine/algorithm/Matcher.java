package com.pratham.backend.engine.algorithm;

import com.pratham.backend.entity.FingerprintRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Matcher {

    private static final int OFFSET_BUCKET_MS = 100;

    public record MatchResult(
            long songId,
            double score
    ) {}

    /**
     * Finds the best matching song using time-offset voting.
     *
     * For every database fingerprint that matches a query
     * fingerprint:
     *
     *      offset =
     *          database anchor time
     *          -
     *          query anchor time
     *
     * The offset is placed into a 100 ms bucket.
     *
     * A real song should produce many votes in the same
     * offset bucket.
     *
     * BUG 2 FIX:
     *
     * There is NO 50% zone-coverage pre-filter.
     *
     * A different recording source may lose many hashes
     * because of:
     *
     *      - compression
     *      - noise
     *      - EQ
     *      - microphone recording
     *      - different encoding
     *
     * The offset histogram should decide whether the
     * remaining hashes form a valid match.
     */
    public static Optional<MatchResult> findBestMatch(
            Map<Integer, Long> sampleFingerprint,
            List<FingerprintRecord> dbMatches
    ) {

        if (sampleFingerprint.isEmpty()
                || dbMatches.isEmpty()) {

            return Optional.empty();
        }

        /*
         * songId
         *      ↓
         * offset bucket
         *      ↓
         * number of votes
         */
        Map<Long, Map<Long, Integer>> votes =
                new HashMap<>();

        for (FingerprintRecord record : dbMatches) {

            /*
             * Find the query timestamp associated with
             * this fingerprint address.
             */
            Long sampleTime =
                    sampleFingerprint.get(
                            record.getAddress()
                    );

            if (sampleTime == null) {
                continue;
            }

            /*
             * Calculate relative time offset.
             */
            long offset =
                    record.getAnchorTimeMs()
                            - sampleTime;

            /*
             * Put the offset into a 100 ms bucket.
             *
             * floorDiv is used instead of normal '/'
             * so negative offsets are handled correctly.
             */
            long bucket =
                    Math.floorDiv(
                            offset,
                            OFFSET_BUCKET_MS
                    );

            /*
             * Add one vote for this song and
             * this offset bucket.
             */
            votes
                    .computeIfAbsent(
                            record.getSongId(),
                            ignored -> new HashMap<>()
                    )
                    .merge(
                            bucket,
                            1,
                            Integer::sum
                    );
        }

        if (votes.isEmpty()) {
            return Optional.empty();
        }

        /*
         * For each song:
         *
         *      score =
         *          highest number of votes
         *          in any single offset bucket
         *
         * Then choose the song with the highest score.
         */
        return votes.entrySet()
                .stream()
                .map(entry -> {

                    int bestBucketScore =
                            entry.getValue()
                                    .values()
                                    .stream()
                                    .mapToInt(
                                            Integer::intValue
                                    )
                                    .max()
                                    .orElse(0);

                    return new MatchResult(
                            entry.getKey(),
                            bestBucketScore
                    );
                })
                .max(
                        java.util.Comparator.comparingDouble(
                                MatchResult::score
                        )
                );
    }
}