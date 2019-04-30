package kademlia_simulator;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

/**
 * Identifier class representing the identifier of a node in kademlia.
 */
public final class Identifier {

    /**
     * BigInteget representing the identifier
     */
    private final BigInteger id;

    /**
     * number of bits of the identifier
     */
    private final int numBits;


    /**
     * Consructs a new random Identifier with the specified number of bits.
     * 
     * The identifier is obtained generating a 512 random bits,
     * applying the sha-256 algorithm on the random generate bits
     * and truncating the result of the sha-256 algorithm to the specified
     * number of bits.
     * 
     * @param numBits  number of bits of the identifier
     */
    public Identifier(final int numBits, final Random random) {
        if(random == null) throw new NullPointerException();

        if(numBits < 0)
            throw new IllegalArgumentException("numBits must be non-negative");
        if(numBits > 256)
            throw new IllegalArgumentException("numBits must not be bigger than 256");
        
        this.numBits = numBits;

        // generate a byte array of 512 random bits
        byte[] bytes = new BigInteger(512, random).toByteArray();

        try {
            // if the sha-256 algorithm is available, apply it to the random byte array
            final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            bytes = sha256.digest(bytes);
            
        } catch (NoSuchAlgorithmException e) {}

        // create a big integer from the byte array truncated to the numBits bits
        this.id = new BigInteger(1, Identifier.truncate(bytes, numBits));
    }


    /**
     * Returns a byte array obtained from the specified byte array truncated to
     * the specified number of bits.
     * 
     * @param  bytes  specified byte array
     * @param  numBits  specified number of bits
     * @return  a new byte array representing the specified byte array truncated to
     *          numBits bits.
     */
    private static final byte[] truncate(final byte[] bytes, final int numBits) {
        if(bytes == null) throw new NullPointerException();

        if (numBits < 0)
            throw new IllegalArgumentException("numBits must be non-negative");
            
        if (numBits > bytes.length * 8)
            throw new IllegalArgumentException("numBits must be smaller then the "
            + "bits in the specified byte array");

        final int numBytes = (int)(((long)numBits+7)/8);

        final byte[] truncatedBytes = Arrays.copyOf(bytes, numBytes);

        final int excessBits = 8*numBytes - numBits;
        truncatedBytes[0] &= (1 << (8-excessBits)) - 1;

        return truncatedBytes;
    }


    /**
     * Consructs a new Identifier with the specified number of bits and the
     * specified BigInteger.
     * 
     * This constructor is used only internally by the class.
     * 
     * @param numBits
     * @param bi
     */
    private Identifier(final int numBits, final BigInteger bi) {
        if(bi == null) throw new NullPointerException();

        if(numBits < 0)
            throw new IllegalArgumentException("numBits must be non-negative");
        if(numBits > 256)
            throw new IllegalArgumentException("numBits must not be bigger than 256");

        this.numBits = numBits;
        this.id = bi;
    }


    /**
     * Returns the distance between this identifier the specified identifier.
     * 
     * @param other  specified identifier
     * @return  the distance between this identifier the specified identifier
     */
    public final BigInteger distance(final Identifier other) {
        if(other == null) throw new NullPointerException();

        // the distance corresponds to the xor between their internal BigInteger
        return this.id.xor(other.id);
    }


    /**
     * Converts this identifier to a BigInteger.
     * 
     * @return  the corrisponding BigInteger to this identifier
     */
    public final BigInteger toBigInteger() {
        return this.id;
    }


    /**
     * Returns a string corresponding to the binary representation of this identifier.
     * 
     * The returned string has numBits characters.
     * 
     * @return  the string corresponding to the binary representation of this identifier
     */
    public final String toString() {
        String result = this.id.toString(2);

        while(result.length() < this.numBits) {
            result = "0" + result;
        }

        return result;
    }


    /**
     * Returns a string corresponding in base n of the number corresponding to 
     * this identifier.
     * 
     * The returned string has the minimal required number of characters.
     * 
     * @param n  specified base in which represents the output
     * @return  the string corresponding to the number corresponding to this 
     *          identifier in the specified base
     */
    public final String toString(int n) {
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
    public final Identifier randomInBucket(final int bucketIndex, Random random) {
        if(random == null) throw new NullPointerException();

        if(bucketIndex < 0)
            throw new IllegalArgumentException("bucketIndex bust be bigger than or equal to 0");

        // generate a random number of (bucketIndex+1) bits and set the most
        // significant bit
        final BigInteger randomFlip = new BigInteger(bucketIndex + 1, random)
                                            .setBit(bucketIndex);

        // return an identifier obtained from the generated random number
        return new Identifier(this.numBits, this.id.xor(randomFlip));
    }


    /**
     * Two identifiers are considered equal if they have the same BigInteger 
     * representation and the same number of bits.
     * 
     * @return  {@true} if this node and obj node have the same BigInteger
     * representation and the same number of bits
     */
    @Override
    public final boolean equals(final Object obj) {
        if(obj == null || obj.getClass() != this.getClass())
            return false;

        Identifier other = (Identifier) obj;

        return this.id.equals(other.id) && this.numBits == other.numBits;
    }


    /**
     * Two identifiers have the same hashCode if they have the same BigInteger 
     * representation and the same number of bits.
     * 
     * @return  the hashCode depending on the BigInteger representation and the
     * number of bits of this identifier
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.numBits);
    }
}