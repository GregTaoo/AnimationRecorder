package top.gregtao.animt;

public class HitBoxHelper {
    public double x, y, z, theta, m, n;
    public double k, width, height;

    public HitBoxHelper(double x, double y, double z, double theta, double m, double n) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.theta = theta;
        this.m = m;
        this.n = n;
        this.k = z / (Math.sqrt(m * m + n * n) * Math.tan(theta) - z / 2);
        this.width = this.width();
        this.height = this.height();
    }

    public double width() {
        return Math.sqrt(this.x * this.x + this.y * this.y) * (this.k + 1);
    }

    public double height() {
        double a = this.m * this.k + this.x * (this.k + 1) / 2;
        double b = this.n * this.k + this.y * (this.k + 1) / 2;
        return Math.sqrt(a * a + b * b);
    }

}
