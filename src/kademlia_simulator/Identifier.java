package kademlia_simulator;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/**
 * Identifier
 */
public final class Identifier {

    private final BigInteger id;
    private final int numBits;

    public Identifier(int numBits, Random random) {
        if(random == null) throw new NullPointerException();

        if(numBits < 0)
            throw new IllegalArgumentException("numBits must be non-negative");
        if(numBits > 256)
            throw new IllegalArgumentException("numBits must not be bigger than 256");
        
        this.numBits = numBits;

        byte[] bytes = new BigInteger(512, random).toByteArray();

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            bytes = sha256.digest(bytes);
            
        } catch (NoSuchAlgorithmException e) {}

        this.id = new BigInteger(1, Identifier.truncate(bytes, numBits));
    }


    private static byte[] truncate(byte[] bytes, int numBits) {
        if (numBits < 0)
            throw new IllegalArgumentException("numBits must be non-negative");

        int numBytes = (int)(((long)numBits+7)/8);

        bytes = Arrays.copyOf(bytes, numBytes);

        int excessBits = 8*numBytes - numBits;
        bytes[0] &= (1 << (8-excessBits)) - 1;

        return bytes;
    }


    private Identifier(int numBits, BigInteger bi) {
        if(bi == null) throw new NullPointerException();

        this.numBits = numBits;
        this.id = bi;
    }


    public BigInteger distance(Identifier other) {
        return this.id.xor(other.id);
    }


    public BigInteger toBigInteger() {
        return this.id;
    }


    public String toString() {
        String result = this.id.toString(2);

        while(result.length() < this.numBits) {
            result = "0" + result;
        }

        return result;
    }


    public String toString(int n) {
        return this.id.toString(n);
    }


    /**
     * Random flip last bits, from the bit at position bucketIndex,
     * flip the bit at position bucketIndex + 1, to generate a new Identifier
     * in the bucketIndex-th bucket
     * 
     * @param bucketIndex  index of the bucket
     * @param random  source of randomness to be used in computing the new
     *                Identifier
     * @return  a new Identifier that is in bucketIndex-th bucket of this
     *          Identifier
     */
    public Identifier randomInBucket(int bucketIndex, Random random) {
        BigInteger randomFlip = new BigInteger(bucketIndex + 1, random);
        randomFlip = randomFlip.setBit(bucketIndex);

        return new Identifier(this.numBits, this.id.xor(randomFlip));
    }


    @Override
    public boolean equals(Object obj) {
        if(obj == null || obj.getClass() != this.getClass())
            return false;

        Identifier other = (Identifier) obj;

        return this.id.equals(other.id) && this.numBits == other.numBits;
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.numBits);
    }
}