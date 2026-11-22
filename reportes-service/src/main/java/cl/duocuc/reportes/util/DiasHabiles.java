package cl.duocuc.reportes.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class DiasHabiles {

    private DiasHabiles() {
    }

    public static int entre(LocalDate desde, LocalDate hasta) {
        int dias = 0;
        LocalDate fecha = desde.plusDays(1);
        while (!fecha.isAfter(hasta)) {
            if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY && fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {
                dias++;
            }
            fecha = fecha.plusDays(1);
        }
        return dias;
    }
}
