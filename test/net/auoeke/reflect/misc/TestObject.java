package net.auoeke.reflect.misc;

import java.util.Random;

public class TestObject {
    public static final Random random = new Random();

    public final int integer = getRandom().nextInt();
    public final long loong = getRandom().nextLong();
    public final boolean bool = getRandom().nextBoolean();
    public final short shrt = (short) getRandom().nextInt();
    public final char character = (char) getRandom().nextInt();
    public final float floatt = getRandom().nextFloat();
    public final double dubble = getRandom().nextDouble();
    public final Object obyect = new IntWrapper(getRandom().nextInt());

    public volatile int integerVolatile = getRandom().nextInt();
    public volatile long loongVolatile = getRandom().nextLong();
    public volatile boolean boolVolatile = getRandom().nextBoolean();
    public volatile short shrtVolatile = (short) getRandom().nextInt();
    public volatile char characterVolatile = (char) getRandom().nextInt();
    public volatile float floattVolatile = getRandom().nextFloat();
    public volatile double dubbleVolatile = getRandom().nextDouble();
    public volatile Object obyectVolatile = new IntWrapper(getRandom().nextInt());

    public static Random getRandom() {
        return random;
    }

    public int getInteger() {
        return this.integer;
    }

    public long getLoong() {
        return this.loong;
    }

    public boolean isBool() {
        return this.bool;
    }

    public short getShrt() {
        return this.shrt;
    }

    public char getCharacter() {
        return this.character;
    }

    public float getFloatt() {
        return this.floatt;
    }

    public double getDubble() {
        return this.dubble;
    }

    public Object getObyect() {
        return this.obyect;
    }

    public int getIntegerVolatile() {
        return this.integerVolatile;
    }

    public void setIntegerVolatile(int integerVolatile) {
        this.integerVolatile = integerVolatile;
    }

    public long getLoongVolatile() {
        return this.loongVolatile;
    }

    public void setLoongVolatile(long loongVolatile) {
        this.loongVolatile = loongVolatile;
    }

    public boolean isBoolVolatile() {
        return this.boolVolatile;
    }

    public void setBoolVolatile(boolean boolVolatile) {
        this.boolVolatile = boolVolatile;
    }

    public short getShrtVolatile() {
        return this.shrtVolatile;
    }

    public void setShrtVolatile(short shrtVolatile) {
        this.shrtVolatile = shrtVolatile;
    }

    public char getCharacterVolatile() {
        return this.characterVolatile;
    }

    public void setCharacterVolatile(char characterVolatile) {
        this.characterVolatile = characterVolatile;
    }

    public float getFloattVolatile() {
        return this.floattVolatile;
    }

    public void setFloattVolatile(float floattVolatile) {
        this.floattVolatile = floattVolatile;
    }

    public double getDubbleVolatile() {
        return this.dubbleVolatile;
    }

    public void setDubbleVolatile(double dubbleVolatile) {
        this.dubbleVolatile = dubbleVolatile;
    }

    public Object getObyectVolatile() {
        return this.obyectVolatile;
    }

    public void setObyectVolatile(Object obyectVolatile) {
        this.obyectVolatile = obyectVolatile;
    }
}
