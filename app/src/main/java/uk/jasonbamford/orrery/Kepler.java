/* ***********  copyright Jason Bamford 2019 -- please visit http://jasonbamford.uk/  *********** */
package uk.jasonbamford.orrery;

public class Kepler {
    static double[][] planets = {
            {0.2056,    0.387099,  0.5084,  0.1223,  0.8435,  3.0507, 87.9691},
            {0.0068,    0.723336,  0.9586,  0.0592,  1.3383,  0.8792, 224.701},
            {0.0167,    1.000003,  1.7966,  0.0000,  0.0000,  6.2400, 365.256},
            {0.0934,    1.523710,  5.0003,  0.0323,  0.8650,  0.3384, 686.971},
            {0.0484,    5.202887,  4.7866,  0.0228,  1.7536,  0.3433, 4332.59},
            {0.0539,    9.536676,  5.9156,  0.0434,  1.9838,  5.5389, 10759.22},
            {0.0473,   19.189165,  1.6919,  0.0135,  1.2918,  2.4833, 30688.5},
            {0.0086,   30.069923,  4.7679,  0.0309,  2.3001,  4.5364, 60182.0},
            {0.2488,   39.482117,  1.9856,  0.2991,  1.9252,  0.2594, 90560.0}
    };

    static PlanetPosition planet_at(int planet, double time) {
        double e = planets[planet][0];  // eccentricity
        double a = planets[planet][1];  // semi-major axis
        double w = planets[planet][2];  // argument of periapsis
        double i = planets[planet][3];  // inclination
        double n = planets[planet][4];  // longitude of ascending node
        double L = planets[planet][5];  // mean longitude
        double T = planets[planet][6];  // orbital time in days

        double M = ((time / T) % 1.0) * Math.PI * 2 + L;  // mean anomaly
        double v = true_anomaly(M, e);
        double r = true_radius(v, e, a);

        double x_ = r * Math.cos(v + w);
        double y_ = r * Math.sin(v + w);

        double x = x_ * Math.cos(n) + y_ * Math.cos(i) * -Math.sin(n);
        double y = y_ * Math.cos(n) * Math.cos(i) + x_ * Math.sin(n);
        double z = y_ * Math.sin(i);

        return new PlanetPosition(x, y, z);
    }

    private static double true_anomaly(double M, double e) {
        double e2 = e * e;
        double e3 = e2 * e;
        double e4 = e3 * e;
        double e5 = e4 * e;

        double v = (M
                + (2 * e   -   e3 / 4   +   5 * e5 / 96) * Math.sin(M)
                + (5 * e2 / 4   -   11 * e4 / 24)        * Math.sin(2 * M)
                + (13 * e3 / 12   -   43 * e5 / 64)      * Math.sin(3 * M)
                + 103 * e4 / 96                          * Math.sin(4 * M)
                + 1097 * e5 / 960                        * Math.sin(5 * M)
        );
        return v;
    }

    private static double true_radius(double v, double e, double a) {
        double r = a * ((1 - e * e) / (1 + e * Math.cos(v)));
        return r;
    }
}
