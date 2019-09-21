package io.p13i.ra.utils;


public class Tuple<X, Y> {

    private final X x;
    private final Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public static <X, Y> Tuple<X, Y> of(X x, Y y) {
        return new Tuple<>(x, y);
    }

    public X x() {
        return x;
    }

    public Y y() {
        return y;
    }

    @Override
    public int hashCode() {
        return x.hashCode() * y.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tuple)) {
            return false;
        }

        Tuple<X, Y> other = (Tuple<X, Y>) obj;
        return this.x.equals(other.x) && this.y.equals(other.y);
    }
}
