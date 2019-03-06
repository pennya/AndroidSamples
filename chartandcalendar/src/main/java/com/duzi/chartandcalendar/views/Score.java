/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.duzi.chartandcalendar.views;

/**
 * 특정 날짜에 습관 강도가 얼마나 높은지를 표시하기 위한 모델
 */
public final class Score
{
    /**
     * 최대값
     */
    static final int MAX_VALUE = 19259478;

    /**
     * UTC로 표현
     */
    private final Long timestamp;

    /**
     * 습관 강도 값
     */
    private final Integer value;

    Score(Long timestamp, Integer value)
    {
        this.timestamp = timestamp;
        this.value = value;
    }

    /**
     * Given the frequency of the habit, the previous score, and the value of
     * the current checkmark, computes the current score for the habit.
     * <p>
     * The frequency of the habit is the number of repetitions divided by the
     * length of the interval. For example, a habit that should be repeated 3
     * times in 8 days has frequency 3.0 / 8.0 = 0.375.
     * <p>
     * The checkmarkValue should be UNCHECKED, CHECKED_IMPLICITLY or
     * CHECK_EXPLICITLY.
     *
     * @param frequency      the frequency of the habit
     * @param previousScore  the previous score of the habit
     * @param checkmarkValue the value of the current checkmark
     * @return the current score
     */
    public static int compute(double frequency,
                              int previousScore,
                              int checkmarkValue)
    {
        double multiplier = Math.pow(0.5, 1.0 / (14.0 / frequency - 1));
        int score = (int) (previousScore * multiplier);

        if (checkmarkValue == Checkmark.CHECKED_EXPLICITLY)
        {
            score += 1000000;
            score = Math.min(score, Score.MAX_VALUE);
        }

        return score;
    }

    public int compareNewer(Score other)
    {
        return Long.signum(this.getTimestamp() - other.getTimestamp());
    }

    public Long getTimestamp()
    {
        return timestamp;
    }

    public Integer getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return new StringBuilder()
                .append("timestamp")
                .append(timestamp)
                .append("value")
                .append(value)
                .toString();
    }
}
