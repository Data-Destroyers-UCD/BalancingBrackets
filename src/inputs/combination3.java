/*
 * Copyright (c) 1996, 2019, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 * Portions Copyright IBM Corporation, 2001. All Rights Reserved.
 */

package java.math;

import static java.math.BigInteger.LONG_MASK;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;
import java.util.Arrays;

/**
 * Immutable, arbitrary-precision signed decimal numbers.  A
 * {@code BigDecimal} consists of an arbitrary precision integer
 * <i>unscaled value</i> and a 32-bit integer <i>scale</i>.  If zero
 * or positive, the scale is the number of digits to the right of the
 * decimal point.  If negative, the unscaled value of the number is
 * multiplied by ten to the power of the negation of the scale.  The
 * value of the number represented by the {@code BigDecimal} is
 * therefore <tt>(unscaledValue &times; 10<sup>-scale</sup>)</tt>.
 *
 * <p>The {@code BigDecimal} class provides operations for
 * arithmetic, scale manipulation, rounding, comparison, hashing, and
 * format conversion.  The {@link #toString} method provides a
 * canonical representation of a {@code BigDecimal}.
 *
 * <p>The {@code BigDecimal} class gives its user complete control
 * over rounding behavior.  If no rounding mode is specified and the
 * exact result cannot be represented, an exception is thrown;
 * otherwise, calculations can be carried out to a chosen precision
 * and rounding mode by supplying an appropriate {@link MathContext}
 * object to the operation.  In either case, eight <em>rounding
 * modes</em> are provided for the control of rounding.  Using the
 * integer fields in this class (such as {@link #ROUND_HALF_UP}) to
 * represent rounding mode is largely obsolete; the enumeration values
 * of the {@code RoundingMode} {@code enum}, (such as {@link
 * RoundingMode#HALF_UP}) should be used instead.
 *
 * <p>When a {@code MathContext} object is supplied with a precision
 * setting of 0 (for example, {@link MathContext#UNLIMITED}),
 * arithmetic operations are exact, as are the arithmetic methods
 * which take no {@code MathContext} object.  (This is the only
 * behavior that was supported in releases prior to 5.)  As a
 * corollary of computing the exact result, the rounding mode setting
 * of a {@code MathContext} object with a precision setting of 0 is
 * not used and thus irrelevant.  In the case of divide, the exact
 * quotient could have an infinitely long decimal expansion; for
 * example, 1 divided by 3.  If the quotient has a nonterminating
 * decimal expansion and the operation is specified to return an exact
 * result, an {@code ArithmeticException} is thrown.  Otherwise, the
 * exact result of the division is returned, as done for other
 * operations.
 *
 * <p>When the precision setting is not 0, the rules of
 * {@code BigDecimal} arithmetic are broadly compatible with selected
 * modes of operation of the arithmetic defined in ANSI X3.274-1996
 * and ANSI X3.274-1996/AM 1-2000 (section 7.4).  Unlike those
 * standards, {@code BigDecimal} includes many rounding modes, which
 * were mandatory for division in {@code BigDecimal} releases prior
 * to 5.  Any conflicts between these ANSI standards and the
 * {@code BigDecimal} specification are resolved in favor of
 * {@code BigDecimal}.
 *
 * <p>Since the same numerical value can have different
 * representations (with different scales), the rules of arithmetic
 * and rounding must specify both the numerical result and the scale
 * used in the result's representation.
 *
 *
 * <p>In general the rounding modes and precision setting determine
 * how operations return results with a limited number of digits when
 * the exact result has more digits (perhaps infinitely many in the
 * case of division) than the number of digits returned.
 *
 * First, the
 * total number of digits to return is specified by the
 * {@code MathContext}'s {@code precision} setting; this determines
 * the result's <i>precision</i>.  The digit count starts from the
 * leftmost nonzero digit of the exact result.  The rounding mode
 * determines how any discarded trailing digits affect the returned
 * result.
 *
 * <p>For all arithmetic operators , the operation is carried out as
 * though an exact intermediate result were first calculated and then
 * rounded to the number of digits specified by the precision setting
 * (if necessary), using the selected rounding mode.  If the exact
 * result is not returned, some digit positions of the exact result
 * are discarded.  When rounding increases the magnitude of the
 * returned result, it is possible for a new digit position to be
 * created by a carry propagating to a leading {@literal "9"} digit.
 * For example, rounding the value 999.9 to three digits rounding up
 * would be numerically equal to one thousand, represented as
 * 100&times;10<sup>1</sup>.  In such cases, the new {@literal "1"} is
 * the leading digit position of the returned result.
 *
 * <p>Besides a logical exact result, each arithmetic operation has a
 * preferred scale for representing a result.  The preferred
 * scale for each operation is listed in the table below.
 *
 * <table border>
 * <caption><b>Preferred Scales for Results of Arithmetic Operations
 * </b></caption>
 * <tr><th>Operation</th><th>Preferred Scale of Result</th></tr>
 * <tr><td>Add</td><td>max(addend.scale(), augend.scale())</td>
 * <tr><td>Subtract</td><td>max(minuend.scale(), subtrahend.scale())</td>
 * <tr><td>Multiply</td><td>multiplier.scale() + multiplicand.scale()</td>
 * <tr><td>Divide</td><td>dividend.scale() - divisor.scale()</td>
 * </table>
 *
 * These scales are the ones used by the methods which return exact
 * arithmetic results; except that an exact divide may have to use a
 * larger scale since the exact result may have more digits.  For
 * example, {@code 1/32} is {@code 0.03125}.
 *
 * <p>Before rounding, the scale of the logical exact intermediate
 * result is the preferred scale for that operation.  If the exact
 * numerical result cannot be represented in {@code precision}
 * digits, rounding selects the set of digits to return and the scale
 * of the result is reduced from the scale of the intermediate result
 * to the least scale which can represent the {@code precision}
 * digits actually returned.  If the exact result can be represented
 * with at most {@code precision} digits, the representation
 * of the result with the scale closest to the preferred scale is
 * returned.  In particular, an exactly representable quotient may be
 * represented in fewer than {@code precision} digits by removing
 * trailing zeros and decreasing the scale.  For example, rounding to
 * three digits using the {@linkplain RoundingMode#FLOOR floor}
 * rounding mode, <br>
 *
 * {@code 19/100 = 0.19   // integer=19,  scale=2} <br>
 *
 * but<br>
 *
 * {@code 21/110 = 0.190  // integer=190, scale=3} <br>
 *
 * <p>Note that for add, subtract, and multiply, the reduction in
 * scale will equal the number of digit positions of the exact result
 * which are discarded. If the rounding causes a carry propagation to
 * create a new high-order digit position, an additional digit of the
 * result is discarded than when no new digit position is created.
 *
 * <p>Other methods may have slightly different rounding semantics.
 * For example, the result of the {@code pow} method using the
 * {@linkplain #pow(int, MathContext) specified algorithm} can
 * occasionally differ from the rounded mathematical result by more
 * than one unit in the last place, one <i>{@linkplain #ulp() ulp}</i>.
 *
 * <p>Two types of operations are provided for manipulating the scale
 * of a {@code BigDecimal}: scaling/rounding operations and decimal
 * point motion operations.  Scaling/rounding operations ({@link
 * #setScale setScale} and {@link #round round}) return a
 * {@code BigDecimal} whose value is approximately (or exactly) equal
 * to that of the operand, but whose scale or precision is the
 * specified value; that is, they increase or decrease the precision
 * of the stored number with minimal effect on its value.  Decimal
 * point motion operations ({@link #movePointLeft movePointLeft} and
 * {@link #movePointRight movePointRight}) return a
 * {@code BigDecimal} created from the operand by moving the decimal
 * point a specified distance in the specified direction.
 *
 * <p>For the sake of brevity and clarity, pseudo-code is used
 * throughout the descriptions of {@code BigDecimal} methods.  The
 * pseudo-code expression {@code (i + j)} is shorthand for "a
 * {@code BigDecimal} whose value is that of the {@code BigDecimal}
 * {@code i} added to that of the {@code BigDecimal}
 * {@code j}." The pseudo-code expression {@code (i == j)} is
 * shorthand for "{@code true} if and only if the
 * {@code BigDecimal} {@code i} represents the same value as the
 * {@code BigDecimal} {@code j}." Other pseudo-code expressions
 * are interpreted similarly.  Square brackets are used to represent
 * the particular {@code BigInteger} and scale pair defining a
 * {@code BigDecimal} value; for example [19, 2] is the
 * {@code BigDecimal} numerically equal to 0.19 having a scale of 2.
 *
 * <p>Note: care should be exercised if {@code BigDecimal} objects
 * are used as keys in a {@link java.util.SortedMap SortedMap} or
 * elements in a {@link java.util.SortedSet SortedSet} since
 * {@code BigDecimal}'s <i>natural ordering</i> is <i>inconsistent
 * with equals</i>.  See {@link Comparable}, {@link
 * java.util.SortedMap} or {@link java.util.SortedSet} for more
 * information.
 *
 * <p>All methods and constructors for this class throw
 * {@code NullPointerException} when passed a {@code null} object
 * reference for any input parameter.
 *
 * @see     BigInteger
 * @see     MathContext
 * @see     RoundingMode
 * @see     java.util.SortedMap
 * @see     java.util.SortedSet
 * @author  Josh Bloch
 * @author  Mike Cowlishaw
 * @author  Joseph D. Darcy
 * @author  Sergey V. Kuksenko
 */
public class BigDecimal extends Number implements Comparable<BigDecimal> {
    /**
     * The unscaled value of this BigDecimal, as returned by {@link
     * #unscaledValue}.
     *
     * @serial
     * @see #unscaledValue
     */
    private final BigInteger intVal;

    /**
     * The scale of this BigDecimal, as returned by {@link #scale}.
     *
     * @serial
     * @see #scale
     */
    private final int scale;  // Note: this may have any value, so
                              // calculations must be done in longs

    /**
     * The number of decimal digits in this BigDecimal, or 0 if the
     * number of digits are not known (lookaside information).  If
     * nonzero, the value is guaranteed correct.  Use the precision()
     * method to obtain and set the value if it might be 0.  This
     * field is mutable until set nonzero.
     *
     * @since  1.5
     */
    private transient int precision;

    /**
     * Used to store the canonical string representation, if computed.
     */
    private transient String stringCache;

    /**
     * Sentinel value for {@link #intCompact} indicating the
     * significand information is only available from {@code intVal}.
     */
    static final long INFLATED = Long.MIN_VALUE;

    private static final BigInteger INFLATED_BIGINT = BigInteger.valueOf(INFLATED);

    /**
     * If the absolute value of the significand of this BigDecimal is
     * less than or equal to {@code Long.MAX_VALUE}, the value can be
     * compactly stored in this field and used in computations.
     */
    private final transient long intCompact;

    // All 18-digit base ten strings fit into a long; not all 19-digit
    // strings will
    private static final int MAX_COMPACT_DIGITS = 18;

    /* Appease the serialization gods */
    private static final long serialVersionUID = 6108874887143696463L;

    private static final ThreadLocal<StringBuilderHelper>
        threadLocalStringBuilderHelper = new ThreadLocal<StringBuilderHelper>() {
        @Override
        protected StringBuilderHelper initialValue() {
            return new StringBuilderHelper();
        }
    };

    // Cache of common small BigDecimal values.
    private static final BigDecimal zeroThroughTen[] = {
        new BigDecimal(BigInteger.ZERO,       0,  0, 1),
        new BigDecimal(BigInteger.ONE,        1,  0, 1),
        new BigDecimal(BigInteger.valueOf(2), 2,  0, 1),
        new BigDecimal(BigInteger.valueOf(3), 3,  0, 1),
        new BigDecimal(BigInteger.valueOf(4), 4,  0, 1),
        new BigDecimal(BigInteger.valueOf(5), 5,  0, 1),
        new BigDecimal(BigInteger.valueOf(6), 6,  0, 1),
        new BigDecimal(BigInteger.valueOf(7), 7,  0, 1),
        new BigDecimal(BigInteger.valueOf(8), 8,  0, 1),
        new BigDecimal(BigInteger.valueOf(9), 9,  0, 1),
        new BigDecimal(BigInteger.TEN,        10, 0, 2),
    };

    // Cache of zero scaled by 0 - 15
    private static final BigDecimal[] ZERO_SCALED_BY = {
        zeroThroughTen[0],
        new BigDecimal(BigInteger.ZERO, 0, 1, 1),
        new BigDecimal(BigInteger.ZERO, 0, 2, 1),
        new BigDecimal(BigInteger.ZERO, 0, 3, 1),
        new BigDecimal(BigInteger.ZERO, 0, 4, 1),
        new BigDecimal(BigInteger.ZERO, 0, 5, 1),
        new BigDecimal(BigInteger.ZERO, 0, 6, 1),
        new BigDecimal(BigInteger.ZERO, 0, 7, 1),
        new BigDecimal(BigInteger.ZERO, 0, 8, 1),
        new BigDecimal(BigInteger.ZERO, 0, 9, 1),
        new BigDecimal(BigInteger.ZERO, 0, 10, 1),
        new BigDecimal(BigInteger.ZERO, 0, 11, 1),
        new BigDecimal(BigInteger.ZERO, 0, 12, 1),
        new BigDecimal(BigInteger.ZERO, 0, 13, 1),
        new BigDecimal(BigInteger.ZERO, 0, 14, 1),
        new BigDecimal(BigInteger.ZERO, 0, 15, 1),
    };

    // Half of Long.MIN_VALUE & Long.MAX_VALUE.
    private static final long HALF_LONG_MAX_VALUE = Long.MAX_VALUE / 2;
    private static final long HALF_LONG_MIN_VALUE = Long.MIN_VALUE / 2;

    // Constants
    /**
     * The value 0, with a scale of 0.
     *
     * @since  1.5
     */
    public static final BigDecimal ZERO =
        zeroThroughTen[0];

    /**
     * The value 1, with a scale of 0.
     *
     * @since  1.5
     */
    public static final BigDecimal ONE =
        zeroThroughTen[1];

    /**
     * The value 10, with a scale of 0.
     *
     * @since  1.5
     */
    public static final BigDecimal TEN =
        zeroThroughTen[10];

    // Constructors

    /**
     * Trusted package private constructor.
     * Trusted simply means if val is INFLATED, intVal could not be null and
     * if intVal is null, val could not be INFLATED.
     */
    BigDecimal(BigInteger intVal, long val, int scale, int prec) {
        this.scale = scale;
        this.precision = prec;
        this.intCompact = val;
        this.intVal = intVal;
    }

    /**
     * Translates a character array representation of a
     * {@code BigDecimal} into a {@code BigDecimal}, accepting the
     * same sequence of characters as the {@link #BigDecimal(String)}
     * constructor, while allowing a sub-array to be specified.
     *
     * <p>Note that if the sequence of characters is already available
     * within a character array, using this constructor is faster than
     * converting the {@code char} array to string and using the
     * {@code BigDecimal(String)} constructor .
     *
     * @param  in {@code char} array that is the source of characters.
     * @param  offset first character in the array to inspect.
     * @param  len number of characters to consider.
     * @throws NumberFormatException if {@code in} is not a valid
     *         representation of a {@code BigDecimal} or the defined subarray
     *         is not wholly within {@code in}.
     * @since  1.5
     */
    public BigDecimal(char[] in, int offset, int len) {
        this(in,offset,len,MathContext.UNLIMITED);
    }

    /**
     * Translates a character array representation of a
     * {@code BigDecimal} into a {@code BigDecimal}, accepting the
     * same sequence of characters as the {@link #BigDecimal(String)}
     * constructor, while allowing a sub-array to be specified and
     * with rounding according to the context settings.
     *
     * <p>Note that if the sequence of characters is already available
     * within a character array, using this constructor is faster than
     * converting the {@code char} array to string and using the
     * {@code BigDecimal(String)} constructor .
     *
     * @param  in {@code char} array that is the source of characters.
     * @param  offset first character in the array to inspect.
     * @param  len number of characters to consider..
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @throws NumberFormatException if {@code in} is not a valid
     *         representation of a {@code BigDecimal} or the defined subarray
     *         is not wholly within {@code in}.
     * @since  1.5
     */
    public BigDecimal(char[] in, int offset, int len, MathContext mc) {
        // protect against huge length, negative values, and integer overflow
        if ((in.length | len | offset) < 0 || len > in.length - offset) {
            throw new NumberFormatException
                ("Bad offset or len arguments for char[] input.");
        }

        // This is the primary string to BigDecimal constructor; all
        // incoming strings end up here; it uses explicit (inline)
        // parsing for speed and generates at most one intermediate
        // (temporary) object (a char[] array) for non-compact case.

        // Use locals for all fields values until completion
        int prec = 0;                 // record precision value
        int scl = 0;                  // record scale value
        long rs = 0;                  // the compact value in long
        BigInteger rb = null;         // the inflated value in BigInteger
        // use array bounds checking to handle too-long, len == 0,
        // bad offset, etc.
        try {
            // handle the sign
            boolean isneg = false;          // assume positive
            if (in[offset] == '-') {
                isneg = true;               // leading minus means negative
                offset++;
                len--;
            } else if (in[offset] == '+') { // leading + allowed
                offset++;
                len--;
            }

            // should now be at numeric part of the significand
            boolean dot = false;             // true when there is a '.'
            long exp = 0;                    // exponent
            char c;                          // current character
            boolean isCompact = (len <= MAX_COMPACT_DIGITS);
            // integer significand array & idx is the index to it. The array
            // is ONLY used when we can't use a compact representation.
            int idx = 0;
            if (isCompact) {
                // First compact case, we need not to preserve the character
                // and we can just compute the value in place.
                for (; len > 0; offset++, len--) {
                    c = in[offset];
                    if ((c == '0')) { // have zero
                        if (prec == 0)
                            prec = 1;
                        else if (rs != 0) {
                            rs *= 10;
                            ++prec;
                        } // else digit is a redundant leading zero
                        if (dot)
                            ++scl;
                    } else if ((c >= '1' && c <= '9')) { // have digit
                        int digit = c - '0';
                        if (prec != 1 || rs != 0)
                            ++prec; // prec unchanged if preceded by 0s
                        rs = rs * 10 + digit;
                        if (dot)
                            ++scl;
                    } else if (c == '.') {   // have dot
                        // have dot
                        if (dot) // two dots
                            throw new NumberFormatException();
                        dot = true;
                    } else if (Character.isDigit(c)) { // slow path
                        int digit = Character.digit(c, 10);
                        if (digit == 0) {
                            if (prec == 0)
                                prec = 1;
                            else if (rs != 0) {
                                rs *= 10;
                                ++prec;
                            } // else digit is a redundant leading zero
                        } else {
                            if (prec != 1 || rs != 0)
                                ++prec; // prec unchanged if preceded by 0s
                            rs = rs * 10 + digit;
                        }
                        if (dot)
                            ++scl;
                    } else if ((c == 'e') || (c == 'E')) {
                        exp = parseExp(in, offset, len);
                        // Next test is required for backwards compatibility
                        if ((int) exp != exp) // overflow
                            throw new NumberFormatException();
                        break; // [saves a test]
                    } else {
                        throw new NumberFormatException();
                    }
                }
                if (prec == 0) // no digits found
                    throw new NumberFormatException();
                // Adjust scale if exp is not zero.
                if (exp != 0) { // had significant exponent
                    scl = adjustScale(scl, exp);
                }
                rs = isneg ? -rs : rs;
                int mcp = mc.precision;
                int drop = prec - mcp; // prec has range [1, MAX_INT], mcp has range [0, MAX_INT];
                                       // therefore, this subtract cannot overflow
                if (mcp > 0 && drop > 0) {  // do rounding
                    while (drop > 0) {
                        scl = checkScaleNonZero((long) scl - drop);
                        rs = divideAndRound(rs, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                        prec = longDigitLength(rs);
                        drop = prec - mcp;
                    }
                }
            } else {
                char coeff[] = new char[len];
                for (; len > 0; offset++, len--) {
                    c = in[offset];
                    // have digit
                    if ((c >= '0' && c <= '9') || Character.isDigit(c)) {
                        // First compact case, we need not to preserve the character
                        // and we can just compute the value in place.
                        if (c == '0' || Character.digit(c, 10) == 0) {
                            if (prec == 0) {
                                coeff[idx] = c;
                                prec = 1;
                            } else if (idx != 0) {
                                coeff[idx++] = c;
                                ++prec;
                            } // else c must be a redundant leading zero
                        } else {
                            if (prec != 1 || idx != 0)
                                ++prec; // prec unchanged if preceded by 0s
                            coeff[idx++] = c;
                        }
                        if (dot)
                            ++scl;
                        continue;
                    }
                    // have dot
                    if (c == '.') {
                        // have dot
                        if (dot) // two dots
                            throw new NumberFormatException();
                        dot = true;
                        continue;
                    }
                    // exponent expected
                    if ((c != 'e') && (c != 'E'))
                        throw new NumberFormatException();
                    exp = parseExp(in, offset, len);
                    // Next test is required for backwards compatibility
                    if ((int) exp != exp) // overflow
                        throw new NumberFormatException();
                    break; // [saves a test]
                }
                // here when no characters left
                if (prec == 0) // no digits found
                    throw new NumberFormatException();
                // Adjust scale if exp is not zero.
                if (exp != 0) { // had significant exponent
                    scl = adjustScale(scl, exp);
                }
                // Remove leading zeros from precision (digits count)
                rb = new BigInteger(coeff, isneg ? -1 : 1, prec);
                rs = compactValFor(rb);
                int mcp = mc.precision;
                if (mcp > 0 && (prec > mcp)) {
                    if (rs == INFLATED) {
                        int drop = prec - mcp;
                        while (drop > 0) {
                            scl = checkScaleNonZero((long) scl - drop);
                            rb = divideAndRoundByTenPow(rb, drop, mc.roundingMode.oldMode);
                            rs = compactValFor(rb);
                            if (rs != INFLATED) {
                                prec = longDigitLength(rs);
                                break;
                            }
                            prec = bigDigitLength(rb);
                            drop = prec - mcp;
                        }
                    }
                    if (rs != INFLATED) {
                        int drop = prec - mcp;
                        while (drop > 0) {
                            scl = checkScaleNonZero((long) scl - drop);
                            rs = divideAndRound(rs, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                            prec = longDigitLength(rs);
                            drop = prec - mcp;
                        }
                        rb = null;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NumberFormatException();
        } catch (NegativeArraySizeException e) {
            throw new NumberFormatException();
        }
        this.scale = scl;
        this.precision = prec;
        this.intCompact = rs;
        this.intVal = rb;
    }

    private int adjustScale(int scl, long exp) {
        long adjustedScale = scl - exp;
        if (adjustedScale > Integer.MAX_VALUE || adjustedScale < Integer.MIN_VALUE)
            throw new NumberFormatException("Scale out of range.");
        scl = (int) adjustedScale;
        return scl;
    }

    /*
     * parse exponent
     */
    private static long parseExp(char[] in, int offset, int len){
        long exp = 0;
        offset++;
        char c = in[offset];
        len--;
        boolean negexp = (c == '-');
        // optional sign
        if (negexp || c == '+') {
            offset++;
            c = in[offset];
            len--;
        }
        if (len <= 0) // no exponent digits
            throw new NumberFormatException();
        // skip leading zeros in the exponent
        while (len > 10 && (c=='0' || (Character.digit(c, 10) == 0))) {
            offset++;
            c = in[offset];
            len--;
        }
        if (len > 10) // too many nonzero exponent digits
            throw new NumberFormatException();
        // c now holds first digit of exponent
        for (;; len--) {
            int v;
            if (c >= '0' && c <= '9') {
                v = c - '0';
            } else {
                v = Character.digit(c, 10);
                if (v < 0) // not a digit
                    throw new NumberFormatException();
            }
            exp = exp * 10 + v;
            if (len == 1)
                break; // that was final character
            offset++;
            c = in[offset];
        }
        if (negexp) // apply sign
            exp = -exp;
        return exp;
    }

    /**
     * Translates a character array representation of a
     * {@code BigDecimal} into a {@code BigDecimal}, accepting the
     * same sequence of characters as the {@link #BigDecimal(String)}
     * constructor.
     *
     * <p>Note that if the sequence of characters is already available
     * as a character array, using this constructor is faster than
     * converting the {@code char} array to string and using the
     * {@code BigDecimal(String)} constructor .
     *
     * @param in {@code char} array that is the source of characters.
     * @throws NumberFormatException if {@code in} is not a valid
     *         representation of a {@code BigDecimal}.
     * @since  1.5
     */
    public BigDecimal(char[] in) {
        this(in, 0, in.length);
    }

    /**
     * Translates a character array representation of a
     * {@code BigDecimal} into a {@code BigDecimal}, accepting the
     * same sequence of characters as the {@link #BigDecimal(String)}
     * constructor and with rounding according to the context
     * settings.
     *
     * <p>Note that if the sequence of characters is already available
     * as a character array, using this constructor is faster than
     * converting the {@code char} array to string and using the
     * {@code BigDecimal(String)} constructor .
     *
     * @param  in {@code char} array that is the source of characters.
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @throws NumberFormatException if {@code in} is not a valid
     *         representation of a {@code BigDecimal}.
     * @since  1.5
     */
    public BigDecimal(char[] in, MathContext mc) {
        this(in, 0, in.length, mc);
    }

    /**
     * Translates the string representation of a {@code BigDecimal}
     * into a {@code BigDecimal}.  The string representation consists
     * of an optional sign, {@code '+'} (<tt> '&#92;u002B'</tt>) or
     * {@code '-'} (<tt>'&#92;u002D'</tt>), followed by a sequence of
     * zero or more decimal digits ("the integer"), optionally
     * followed by a fraction, optionally followed by an exponent.
     *
     * <p>The fraction consists of a decimal point followed by zero
     * or more decimal digits.  The string must contain at least one
     * digit in either the integer or the fraction.  The number formed
     * by the sign, the integer and the fraction is referred to as the
     * <i>significand</i>.
     *
     * <p>The exponent consists of the character {@code 'e'}
     * (<tt>'&#92;u0065'</tt>) or {@code 'E'} (<tt>'&#92;u0045'</tt>)
     * followed by one or more decimal digits.  The value of the
     * exponent must lie between -{@link Integer#MAX_VALUE} ({@link
     * Integer#MIN_VALUE}+1) and {@link Integer#MAX_VALUE}, inclusive.
     *
     * <p>More formally, the strings this constructor accepts are
     * described by the following grammar:
     * <blockquote>
     * <dl>
     * <dt><i>BigDecimalString:</i>
     * <dd><i>Sign<sub>opt</sub> Significand Exponent<sub>opt</sub></i>
     * <dt><i>Sign:</i>
     * <dd>{@code +}
     * <dd>{@code -}
     * <dt><i>Significand:</i>
     * <dd><i>IntegerPart</i> {@code .} <i>FractionPart<sub>opt</sub></i>
     * <dd>{@code .} <i>FractionPart</i>
     * <dd><i>IntegerPart</i>
     * <dt><i>IntegerPart:</i>
     * <dd><i>Digits</i>
     * <dt><i>FractionPart:</i>
     * <dd><i>Digits</i>
     * <dt><i>Exponent:</i>
     * <dd><i>ExponentIndicator SignedInteger</i>
     * <dt><i>ExponentIndicator:</i>
     * <dd>{@code e}
     * <dd>{@code E}
     * <dt><i>SignedInteger:</i>
     * <dd><i>Sign<sub>opt</sub> Digits</i>
     * <dt><i>Digits:</i>
     * <dd><i>Digit</i>
     * <dd><i>Digits Digit</i>
     * <dt><i>Digit:</i>
     * <dd>any character for which {@link Character#isDigit}
     * returns {@code true}, including 0, 1, 2 ...
     * </dl>
     * </blockquote>
     *
     * <p>The scale of the returned {@code BigDecimal} will be the
     * number of digits in the fraction, or zero if the string
     * contains no decimal point, subject to adjustment for any
     * exponent; if the string contains an exponent, the exponent is
     * subtracted from the scale.  The value of the resulting scale
     * must lie between {@code Integer.MIN_VALUE} and
     * {@code Integer.MAX_VALUE}, inclusive.
     *
     * <p>The character-to-digit mapping is provided by {@link
     * java.lang.Character#digit} set to convert to radix 10.  The
     * String may not contain any extraneous characters (whitespace,
     * for example).
     *
     * <p><b>Examples:</b><br>
     * The value of the returned {@code BigDecimal} is equal to
     * <i>significand</i> &times; 10<sup>&nbsp;<i>exponent</i></sup>.
     * For each string on the left, the resulting representation
     * [{@code BigInteger}, {@code scale}] is shown on the right.
     * <pre>
     * "0"            [0,0]
     * "0.00"         [0,2]
     * "123"          [123,0]
     * "-123"         [-123,0]
     * "1.23E3"       [123,-1]
     * "1.23E+3"      [123,-1]
     * "12.3E+7"      [123,-6]
     * "12.0"         [120,1]
     * "12.3"         [123,1]
     * "0.00123"      [123,5]
     * "-1.23E-12"    [-123,14]
     * "1234.5E-4"    [12345,5]
     * "0E+7"         [0,-7]
     * "-0"           [0,0]
     * </pre>
     *
     * <p>Note: For values other than {@code float} and
     * {@code double} NaN and &plusmn;Infinity, this constructor is
     * compatible with the values returned by {@link Float#toString}
     * and {@link Double#toString}.  This is generally the preferred
     * way to convert a {@code float} or {@code double} into a
     * BigDecimal, as it doesn't suffer from the unpredictability of
     * the {@link #BigDecimal(double)} constructor.
     *
     * @param val String representation of {@code BigDecimal}.
     *
     * @throws NumberFormatException if {@code val} is not a valid
     *         representation of a {@code BigDecimal}.
     */
    public BigDecimal(String val) {
        this(val.toCharArray(), 0, val.length());
    }

    /**
     * Translates the string representation of a {@code BigDecimal}
     * into a {@code BigDecimal}, accepting the same strings as the
     * {@link #BigDecimal(String)} constructor, with rounding
     * according to the context settings.
     *
     * @param  val string representation of a {@code BigDecimal}.
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @throws NumberFormatException if {@code val} is not a valid
     *         representation of a BigDecimal.
     * @since  1.5
     */
    public BigDecimal(String val, MathContext mc) {
        this(val.toCharArray(), 0, val.length(), mc);
    }

    /**
     * Translates a {@code double} into a {@code BigDecimal} which
     * is the exact decimal representation of the {@code double}'s
     * binary floating-point value.  The scale of the returned
     * {@code BigDecimal} is the smallest value such that
     * <tt>(10<sup>scale</sup> &times; val)</tt> is an integer.
     * <p>
     * <b>Notes:</b>
     * <ol>
     * <li>
     * The results of this constructor can be somewhat unpredictable.
     * One might assume that writing {@code new BigDecimal(0.1)} in
     * Java creates a {@code BigDecimal} which is exactly equal to
     * 0.1 (an unscaled value of 1, with a scale of 1), but it is
     * actually equal to
     * 0.1000000000000000055511151231257827021181583404541015625.
     * This is because 0.1 cannot be represented exactly as a
     * {@code double} (or, for that matter, as a binary fraction of
     * any finite length).  Thus, the value that is being passed
     * <i>in</i> to the constructor is not exactly equal to 0.1,
     * appearances notwithstanding.
     *
     * <li>
     * The {@code String} constructor, on the other hand, is
     * perfectly predictable: writing {@code new BigDecimal("0.1")}
     * creates a {@code BigDecimal} which is <i>exactly</i> equal to
     * 0.1, as one would expect.  Therefore, it is generally
     * recommended that the {@linkplain #BigDecimal(String)
     * <tt>String</tt> constructor} be used in preference to this one.
     *
     * <li>
     * When a {@code double} must be used as a source for a
     * {@code BigDecimal}, note that this constructor provides an
     * exact conversion; it does not give the same result as
     * converting the {@code double} to a {@code String} using the
     * {@link Double#toString(double)} method and then using the
     * {@link #BigDecimal(String)} constructor.  To get that result,
     * use the {@code static} {@link #valueOf(double)} method.
     * </ol>
     *
     * @param val {@code double} value to be converted to
     *        {@code BigDecimal}.
     * @throws NumberFormatException if {@code val} is infinite or NaN.
     */
    public BigDecimal(double val) {
        this(val,MathContext.UNLIMITED);
    }

    /**
     * Translates a {@code double} into a {@code BigDecimal}, with
     * rounding according to the context settings.  The scale of the
     * {@code BigDecimal} is the smallest value such that
     * <tt>(10<sup>scale</sup> &times; val)</tt> is an integer.
     *
     * <p>The results of this constructor can be somewhat unpredictable
     * and its use is generally not recommended; see the notes under
     * the {@link #BigDecimal(double)} constructor.
     *
     * @param  val {@code double} value to be converted to
     *         {@code BigDecimal}.
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         RoundingMode is UNNECESSARY.
     * @throws NumberFormatException if {@code val} is infinite or NaN.
     * @since  1.5
     */
    public BigDecimal(double val, MathContext mc) {
        if (Double.isInfinite(val) || Double.isNaN(val))
            throw new NumberFormatException("Infinite or NaN");
        // Translate the double into sign, exponent and significand, according
        // to the formulae in JLS, Section 20.10.22.
        long valBits = Double.doubleToLongBits(val);
        int sign = ((valBits >> 63) == 0 ? 1 : -1);
        int exponent = (int) ((valBits >> 52) & 0x7ffL);
        long significand = (exponent == 0
                ? (valBits & ((1L << 52) - 1)) << 1
                : (valBits & ((1L << 52) - 1)) | (1L << 52));
        exponent -= 1075;
        // At this point, val == sign * significand * 2**exponent.

        /*
         * Special case zero to supress nonterminating normalization and bogus
         * scale calculation.
         */
        if (significand == 0) {
            this.intVal = BigInteger.ZERO;
            this.scale = 0;
            this.intCompact = 0;
            this.precision = 1;
            return;
        }
        // Normalize
        while ((significand & 1) == 0) { // i.e., significand is even
            significand >>= 1;
            exponent++;
        }
        int scale = 0;
        // Calculate intVal and scale
        BigInteger intVal;
        long compactVal = sign * significand;
        if (exponent == 0) {
            intVal = (compactVal == INFLATED) ? INFLATED_BIGINT : null;
        } else {
            if (exponent < 0) {
                intVal = BigInteger.valueOf(5).pow(-exponent).multiply(compactVal);
                scale = -exponent;
            } else { //  (exponent > 0)
                intVal = BigInteger.valueOf(2).pow(exponent).multiply(compactVal);
            }
            compactVal = compactValFor(intVal);
        }
        int prec = 0;
        int mcp = mc.precision;
        if (mcp > 0) { // do rounding
            int mode = mc.roundingMode.oldMode;
            int drop;
            if (compactVal == INFLATED) {
                prec = bigDigitLength(intVal);
                drop = prec - mcp;
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    intVal = divideAndRoundByTenPow(intVal, drop, mode);
                    compactVal = compactValFor(intVal);
                    if (compactVal != INFLATED) {
                        break;
                    }
                    prec = bigDigitLength(intVal);
                    drop = prec - mcp;
                }
            }
            if (compactVal != INFLATED) {
                prec = longDigitLength(compactVal);
                drop = prec - mcp;
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                    prec = longDigitLength(compactVal);
                    drop = prec - mcp;
                }
                intVal = null;
            }
        }
        this.intVal = intVal;
        this.intCompact = compactVal;
        this.scale = scale;
        this.precision = prec;
    }

    /**
     * Accept no subclasses.
     */
    private static BigInteger toStrictBigInteger(BigInteger val) {
        return (val.getClass() == BigInteger.class) ?
            val :
            new BigInteger(val.toByteArray().clone());
    }

    /**
     * Translates a {@code BigInteger} into a {@code BigDecimal}.
     * The scale of the {@code BigDecimal} is zero.
     *
     * @param val {@code BigInteger} value to be converted to
     *            {@code BigDecimal}.
     */
    public BigDecimal(BigInteger val) {
        scale = 0;
        intVal = toStrictBigInteger(val);
        intCompact = compactValFor(intVal);
    }

    /**
     * Translates a {@code BigInteger} into a {@code BigDecimal}
     * rounding according to the context settings.  The scale of the
     * {@code BigDecimal} is zero.
     *
     * @param val {@code BigInteger} value to be converted to
     *            {@code BigDecimal}.
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal(BigInteger val, MathContext mc) {
        this(toStrictBigInteger(val), 0, mc);
    }

    /**
     * Translates a {@code BigInteger} unscaled value and an
     * {@code int} scale into a {@code BigDecimal}.  The value of
     * the {@code BigDecimal} is
     * <tt>(unscaledVal &times; 10<sup>-scale</sup>)</tt>.
     *
     * @param unscaledVal unscaled value of the {@code BigDecimal}.
     * @param scale scale of the {@code BigDecimal}.
     */
    public BigDecimal(BigInteger unscaledVal, int scale) {
        // Negative scales are now allowed
        this.intVal = toStrictBigInteger(unscaledVal);
        this.intCompact = compactValFor(this.intVal);
        this.scale = scale;
    }

    /**
     * Translates a {@code BigInteger} unscaled value and an
     * {@code int} scale into a {@code BigDecimal}, with rounding
     * according to the context settings.  The value of the
     * {@code BigDecimal} is <tt>(unscaledVal &times;
     * 10<sup>-scale</sup>)</tt>, rounded according to the
     * {@code precision} and rounding mode settings.
     *
     * @param  unscaledVal unscaled value of the {@code BigDecimal}.
     * @param  scale scale of the {@code BigDecimal}.
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal(BigInteger unscaledVal, int scale, MathContext mc) {
        unscaledVal = toStrictBigInteger(unscaledVal);
        long compactVal = compactValFor(unscaledVal);
        int mcp = mc.precision;
        int prec = 0;
        if (mcp > 0) { // do rounding
            int mode = mc.roundingMode.oldMode;
            if (compactVal == INFLATED) {
                prec = bigDigitLength(unscaledVal);
                int drop = prec - mcp;
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    unscaledVal = divideAndRoundByTenPow(unscaledVal, drop, mode);
                    compactVal = compactValFor(unscaledVal);
                    if (compactVal != INFLATED) {
                        break;
                    }
                    prec = bigDigitLength(unscaledVal);
                    drop = prec - mcp;
                }
            }
            if (compactVal != INFLATED) {
                prec = longDigitLength(compactVal);
                int drop = prec - mcp;     // drop can't be more than 18
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mode);
                    prec = longDigitLength(compactVal);
                    drop = prec - mcp;
                }
                unscaledVal = null;
            }
        }
        this.intVal = unscaledVal;
        this.intCompact = compactVal;
        this.scale = scale;
        this.precision = prec;
    }

    /**
     * Translates an {@code int} into a {@code BigDecimal}.  The
     * scale of the {@code BigDecimal} is zero.
     *
     * @param val {@code int} value to be converted to
     *            {@code BigDecimal}.
     * @since  1.5
     */
    public BigDecimal(int val) {
        this.intCompact = val;
        this.scale = 0;
        this.intVal = null;
    }

    /**
     * Translates an {@code int} into a {@code BigDecimal}, with
     * rounding according to the context settings.  The scale of the
     * {@code BigDecimal}, before any rounding, is zero.
     *
     * @param  val {@code int} value to be converted to {@code BigDecimal}.
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal(int val, MathContext mc) {
        int mcp = mc.precision;
        long compactVal = val;
        int scale = 0;
        int prec = 0;
        if (mcp > 0) { // do rounding
            prec = longDigitLength(compactVal);
            int drop = prec - mcp; // drop can't be more than 18
            while (drop > 0) {
                scale = checkScaleNonZero((long) scale - drop);
                compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                prec = longDigitLength(compactVal);
                drop = prec - mcp;
            }
        }
        this.intVal = null;
        this.intCompact = compactVal;
        this.scale = scale;
        this.precision = prec;
    }

    /**
     * Translates a {@code long} into a {@code BigDecimal}.  The
     * scale of the {@code BigDecimal} is zero.
     *
     * @param val {@code long} value to be converted to {@code BigDecimal}.
     * @since  1.5
     */
    public BigDecimal(long val) {
        this.intCompact = val;
        this.intVal = (val == INFLATED) ? INFLATED_BIGINT : null;
        this.scale = 0;
    }

    /**
     * Translates a {@code long} into a {@code BigDecimal}, with
     * rounding according to the context settings.  The scale of the
     * {@code BigDecimal}, before any rounding, is zero.
     *
     * @param  val {@code long} value to be converted to {@code BigDecimal}.
     * @param  mc the context to use.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal(long val, MathContext mc) {
        int mcp = mc.precision;
        int mode = mc.roundingMode.oldMode;
        int prec = 0;
        int scale = 0;
        BigInteger intVal = (val == INFLATED) ? INFLATED_BIGINT : null;
        if (mcp > 0) { // do rounding
            if (val == INFLATED) {
                prec = 19;
                int drop = prec - mcp;
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    intVal = divideAndRoundByTenPow(intVal, drop, mode);
                    val = compactValFor(intVal);
                    if (val != INFLATED) {
                        break;
                    }
                    prec = bigDigitLength(intVal);
                    drop = prec - mcp;
                }
            }
            if (val != INFLATED) {
                prec = longDigitLength(val);
                int drop = prec - mcp;
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    val = divideAndRound(val, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                    prec = longDigitLength(val);
                    drop = prec - mcp;
                }
                intVal = null;
            }
        }
        this.intVal = intVal;
        this.intCompact = val;
        this.scale = scale;
        this.precision = prec;
    }

    // Static Factory Methods

    /**
     * Translates a {@code long} unscaled value and an
     * {@code int} scale into a {@code BigDecimal}.  This
     * {@literal "static factory method"} is provided in preference to
     * a ({@code long}, {@code int}) constructor because it
     * allows for reuse of frequently used {@code BigDecimal} values..
     *
     * @param unscaledVal unscaled value of the {@code BigDecimal}.
     * @param scale scale of the {@code BigDecimal}.
     * @return a {@code BigDecimal} whose value is
     *         <tt>(unscaledVal &times; 10<sup>-scale</sup>)</tt>.
     */
    public static BigDecimal valueOf(long unscaledVal, int scale) {
        if (scale == 0)
            return valueOf(unscaledVal);
        else if (unscaledVal == 0) {
            return zeroValueOf(scale);
        }
        return new BigDecimal(unscaledVal == INFLATED ?
                              INFLATED_BIGINT : null,
                              unscaledVal, scale, 0);
    }

    /**
     * Translates a {@code long} value into a {@code BigDecimal}
     * with a scale of zero.  This {@literal "static factory method"}
     * is provided in preference to a ({@code long}) constructor
     * because it allows for reuse of frequently used
     * {@code BigDecimal} values.
     *
     * @param val value of the {@code BigDecimal}.
     * @return a {@code BigDecimal} whose value is {@code val}.
     */
    public static BigDecimal valueOf(long val) {
        if (val >= 0 && val < zeroThroughTen.length)
            return zeroThroughTen[(int)val];
        else if (val != INFLATED)
            return new BigDecimal(null, val, 0, 0);
        return new BigDecimal(INFLATED_BIGINT, val, 0, 0);
    }

    static BigDecimal valueOf(long unscaledVal, int scale, int prec) {
        if (scale == 0 && unscaledVal >= 0 && unscaledVal < zeroThroughTen.length) {
            return zeroThroughTen[(int) unscaledVal];
        } else if (unscaledVal == 0) {
            return zeroValueOf(scale);
        }
        return new BigDecimal(unscaledVal == INFLATED ? INFLATED_BIGINT : null,
                unscaledVal, scale, prec);
    }

    static BigDecimal valueOf(BigInteger intVal, int scale, int prec) {
        long val = compactValFor(intVal);
        if (val == 0) {
            return zeroValueOf(scale);
        } else if (scale == 0 && val >= 0 && val < zeroThroughTen.length) {
            return zeroThroughTen[(int) val];
        }
        return new BigDecimal(intVal, val, scale, prec);
    }

    static BigDecimal zeroValueOf(int scale) {
        if (scale >= 0 && scale < ZERO_SCALED_BY.length)
            return ZERO_SCALED_BY[scale];
        else
            return new BigDecimal(BigInteger.ZERO, 0, scale, 1);
    }

    /**
     * Translates a {@code double} into a {@code BigDecimal}, using
     * the {@code double}'s canonical string representation provided
     * by the {@link Double#toString(double)} method.
     *
     * <p><b>Note:</b> This is generally the preferred way to convert
     * a {@code double} (or {@code float}) into a
     * {@code BigDecimal}, as the value returned is equal to that
     * resulting from constructing a {@code BigDecimal} from the
     * result of using {@link Double#toString(double)}.
     *
     * @param  val {@code double} to convert to a {@code BigDecimal}.
     * @return a {@code BigDecimal} whose value is equal to or approximately
     *         equal to the value of {@code val}.
     * @throws NumberFormatException if {@code val} is infinite or NaN.
     * @since  1.5
     */
    public static BigDecimal valueOf(double val) {
        // Reminder: a zero double returns '0.0', so we cannot fastpath
        // to use the constant ZERO.  This might be important enough to
        // justify a factory approach, a cache, or a few private
        // constants, later.
        return new BigDecimal(Double.toString(val));
    }

    // Arithmetic Operations
    /**
     * Returns a {@code BigDecimal} whose value is {@code (this +
     * augend)}, and whose scale is {@code max(this.scale(),
     * augend.scale())}.
     *
     * @param  augend value to be added to this {@code BigDecimal}.
     * @return {@code this + augend}
     */
    public BigDecimal add(BigDecimal augend) {
        if (this.intCompact != INFLATED) {
            if ((augend.intCompact != INFLATED)) {
                return add(this.intCompact, this.scale, augend.intCompact, augend.scale);
            } else {
                return add(this.intCompact, this.scale, augend.intVal, augend.scale);
            }
        } else {
            if ((augend.intCompact != INFLATED)) {
                return add(augend.intCompact, augend.scale, this.intVal, this.scale);
            } else {
                return add(this.intVal, this.scale, augend.intVal, augend.scale);
            }
        }
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this + augend)},
     * with rounding according to the context settings.
     *
     * If either number is zero and the precision setting is nonzero then
     * the other number, rounded if necessary, is used as the result.
     *
     * @param  augend value to be added to this {@code BigDecimal}.
     * @param  mc the context to use.
     * @return {@code this + augend}, rounded as necessary.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal add(BigDecimal augend, MathContext mc) {
        if (mc.precision == 0)
            return add(augend);
        BigDecimal lhs = this;

        // If either number is zero then the other number, rounded and
        // scaled if necessary, is used as the result.
        {
            boolean lhsIsZero = lhs.signum() == 0;
            boolean augendIsZero = augend.signum() == 0;

            if (lhsIsZero || augendIsZero) {
                int preferredScale = Math.max(lhs.scale(), augend.scale());
                BigDecimal result;

                if (lhsIsZero && augendIsZero)
                    return zeroValueOf(preferredScale);
                result = lhsIsZero ? doRound(augend, mc) : doRound(lhs, mc);

                if (result.scale() == preferredScale)
                    return result;
                else if (result.scale() > preferredScale) {
                    return stripZerosToMatchScale(result.intVal, result.intCompact, result.scale, preferredScale);
                } else { // result.scale < preferredScale
                    int precisionDiff = mc.precision - result.precision();
                    int scaleDiff     = preferredScale - result.scale();

                    if (precisionDiff >= scaleDiff)
                        return result.setScale(preferredScale); // can achieve target scale
                    else
                        return result.setScale(result.scale() + precisionDiff);
                }
            }
        }

        long padding = (long) lhs.scale - augend.scale;
        if (padding != 0) { // scales differ; alignment needed
            BigDecimal arg[] = preAlign(lhs, augend, padding, mc);
            matchScale(arg);
            lhs = arg[0];
            augend = arg[1];
        }
        return doRound(lhs.inflated().add(augend.inflated()), lhs.scale, mc);
    }

    /**
     * Returns an array of length two, the sum of whose entries is
     * equal to the rounded sum of the {@code BigDecimal} arguments.
     *
     * <p>If the digit positions of the arguments have a sufficient
     * gap between them, the value smaller in magnitude can be
     * condensed into a {@literal "sticky bit"} and the end result will
     * round the same way <em>if</em> the precision of the final
     * result does not include the high order digit of the small
     * magnitude operand.
     *
     * <p>Note that while strictly speaking this is an optimization,
     * it makes a much wider range of additions practical.
     *
     * <p>This corresponds to a pre-shift operation in a fixed
     * precision floating-point adder; this method is complicated by
     * variable precision of the result as determined by the
     * MathContext.  A more nuanced operation could implement a
     * {@literal "right shift"} on the smaller magnitude operand so
     * that the number of digits of the smaller operand could be
     * reduced even though the significands partially overlapped.
     */
    private BigDecimal[] preAlign(BigDecimal lhs, BigDecimal augend, long padding, MathContext mc) {
        assert padding != 0;
        BigDecimal big;
        BigDecimal small;

        if (padding < 0) { // lhs is big; augend is small
            big = lhs;
            small = augend;
        } else { // lhs is small; augend is big
            big = augend;
            small = lhs;
        }

        /*
         * This is the estimated scale of an ulp of the result; it assumes that
         * the result doesn't have a carry-out on a true add (e.g. 999 + 1 =>
         * 1000) or any subtractive cancellation on borrowing (e.g. 100 - 1.2 =>
         * 98.8)
         */
        long estResultUlpScale = (long) big.scale - big.precision() + mc.precision;

        /*
         * The low-order digit position of big is big.scale().  This
         * is true regardless of whether big has a positive or
         * negative scale.  The high-order digit position of small is
         * small.scale - (small.precision() - 1).  To do the full
         * condensation, the digit positions of big and small must be
         * disjoint *and* the digit positions of small should not be
         * directly visible in the result.
         */
        long smallHighDigitPos = (long) small.scale - small.precision() + 1;
        if (smallHighDigitPos > big.scale + 2 && // big and small disjoint
            smallHighDigitPos > estResultUlpScale + 2) { // small digits not visible
            small = BigDecimal.valueOf(small.signum(), this.checkScale(Math.max(big.scale, estResultUlpScale) + 3));
        }

        // Since addition is symmetric, preserving input order in
        // returned operands doesn't matter
        BigDecimal[] result = {big, small};
        return result;
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this -
     * subtrahend)}, and whose scale is {@code max(this.scale(),
     * subtrahend.scale())}.
     *
     * @param  subtrahend value to be subtracted from this {@code BigDecimal}.
     * @return {@code this - subtrahend}
     */
    public BigDecimal subtract(BigDecimal subtrahend) {
        if (this.intCompact != INFLATED) {
            if ((subtrahend.intCompact != INFLATED)) {
                return add(this.intCompact, this.scale, -subtrahend.intCompact, subtrahend.scale);
            } else {
                return add(this.intCompact, this.scale, subtrahend.intVal.negate(), subtrahend.scale);
            }
        } else {
            if ((subtrahend.intCompact != INFLATED)) {
                // Pair of subtrahend values given before pair of
                // values from this BigDecimal to avoid need for
                // method overloading on the specialized add method
                return add(-subtrahend.intCompact, subtrahend.scale, this.intVal, this.scale);
            } else {
                return add(this.intVal, this.scale, subtrahend.intVal.negate(), subtrahend.scale);
            }
        }
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this - subtrahend)},
     * with rounding according to the context settings.
     *
     * If {@code subtrahend} is zero then this, rounded if necessary, is used as the
     * result.  If this is zero then the result is {@code subtrahend.negate(mc)}.
     *
     * @param  subtrahend value to be subtracted from this {@code BigDecimal}.
     * @param  mc the context to use.
     * @return {@code this - subtrahend}, rounded as necessary.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal subtract(BigDecimal subtrahend, MathContext mc) {
        if (mc.precision == 0)
            return subtract(subtrahend);
        // share the special rounding code in add()
        return add(subtrahend.negate(), mc);
    }

    /**
     * Returns a {@code BigDecimal} whose value is <tt>(this &times;
     * multiplicand)</tt>, and whose scale is {@code (this.scale() +
     * multiplicand.scale())}.
     *
     * @param  multiplicand value to be multiplied by this {@code BigDecimal}.
     * @return {@code this * multiplicand}
     */
    public BigDecimal multiply(BigDecimal multiplicand) {
        int productScale = checkScale((long) scale + multiplicand.scale);
        if (this.intCompact != INFLATED) {
            if ((multiplicand.intCompact != INFLATED)) {
                return multiply(this.intCompact, multiplicand.intCompact, productScale);
            } else {
                return multiply(this.intCompact, multiplicand.intVal, productScale);
            }
        } else {
            if ((multiplicand.intCompact != INFLATED)) {
                return multiply(multiplicand.intCompact, this.intVal, productScale);
            } else {
                return multiply(this.intVal, multiplicand.intVal, productScale);
            }
        }
    }

    /**
     * Returns a {@code BigDecimal} whose value is <tt>(this &times;
     * multiplicand)</tt>, with rounding according to the context settings.
     *
     * @param  multiplicand value to be multiplied by this {@code BigDecimal}.
     * @param  mc the context to use.
     * @return {@code this * multiplicand}, rounded as necessary.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal multiply(BigDecimal multiplicand, MathContext mc) {
        if (mc.precision == 0)
            return multiply(multiplicand);
        int productScale = checkScale((long) scale + multiplicand.scale);
        if (this.intCompact != INFLATED) {
            if ((multiplicand.intCompact != INFLATED)) {
                return multiplyAndRound(this.intCompact, multiplicand.intCompact, productScale, mc);
            } else {
                return multiplyAndRound(this.intCompact, multiplicand.intVal, productScale, mc);
            }
        } else {
            if ((multiplicand.intCompact != INFLATED)) {
                return multiplyAndRound(multiplicand.intCompact, this.intVal, productScale, mc);
            } else {
                return multiplyAndRound(this.intVal, multiplicand.intVal, productScale, mc);
            }
        }
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, and whose scale is as specified.  If rounding must
     * be performed to generate a result with the specified scale, the
     * specified rounding mode is applied.
     *
     * <p>The new {@link #divide(BigDecimal, int, RoundingMode)} method
     * should be used in preference to this legacy method.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @param  scale scale of the {@code BigDecimal} quotient to be returned.
     * @param  roundingMode rounding mode to apply.
     * @return {@code this / divisor}
     * @throws ArithmeticException if {@code divisor} is zero,
     *         {@code roundingMode==ROUND_UNNECESSARY} and
     *         the specified scale is insufficient to represent the result
     *         of the division exactly.
     * @throws IllegalArgumentException if {@code roundingMode} does not
     *         represent a valid rounding mode.
     * @see    #ROUND_UP
     * @see    #ROUND_DOWN
     * @see    #ROUND_CEILING
     * @see    #ROUND_FLOOR
     * @see    #ROUND_HALF_UP
     * @see    #ROUND_HALF_DOWN
     * @see    #ROUND_HALF_EVEN
     * @see    #ROUND_UNNECESSARY
     */
    public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode) {
        if (roundingMode < ROUND_UP || roundingMode > ROUND_UNNECESSARY)
            throw new IllegalArgumentException("Invalid rounding mode");
        if (this.intCompact != INFLATED) {
            if ((divisor.intCompact != INFLATED)) {
                return divide(this.intCompact, this.scale, divisor.intCompact, divisor.scale, scale, roundingMode);
            } else {
                return divide(this.intCompact, this.scale, divisor.intVal, divisor.scale, scale, roundingMode);
            }
        } else {
            if ((divisor.intCompact != INFLATED)) {
                return divide(this.intVal, this.scale, divisor.intCompact, divisor.scale, scale, roundingMode);
            } else {
                return divide(this.intVal, this.scale, divisor.intVal, divisor.scale, scale, roundingMode);
            }
        }
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, and whose scale is as specified.  If rounding must
     * be performed to generate a result with the specified scale, the
     * specified rounding mode is applied.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @param  scale scale of the {@code BigDecimal} quotient to be returned.
     * @param  roundingMode rounding mode to apply.
     * @return {@code this / divisor}
     * @throws ArithmeticException if {@code divisor} is zero,
     *         {@code roundingMode==RoundingMode.UNNECESSARY} and
     *         the specified scale is insufficient to represent the result
     *         of the division exactly.
     * @since 1.5
     */
    public BigDecimal divide(BigDecimal divisor, int scale, RoundingMode roundingMode) {
        return divide(divisor, scale, roundingMode.oldMode);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, and whose scale is {@code this.scale()}.  If
     * rounding must be performed to generate a result with the given
     * scale, the specified rounding mode is applied.
     *
     * <p>The new {@link #divide(BigDecimal, RoundingMode)} method
     * should be used in preference to this legacy method.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @param  roundingMode rounding mode to apply.
     * @return {@code this / divisor}
     * @throws ArithmeticException if {@code divisor==0}, or
     *         {@code roundingMode==ROUND_UNNECESSARY} and
     *         {@code this.scale()} is insufficient to represent the result
     *         of the division exactly.
     * @throws IllegalArgumentException if {@code roundingMode} does not
     *         represent a valid rounding mode.
     * @see    #ROUND_UP
     * @see    #ROUND_DOWN
     * @see    #ROUND_CEILING
     * @see    #ROUND_FLOOR
     * @see    #ROUND_HALF_UP
     * @see    #ROUND_HALF_DOWN
     * @see    #ROUND_HALF_EVEN
     * @see    #ROUND_UNNECESSARY
     */
    public BigDecimal divide(BigDecimal divisor, int roundingMode) {
        return this.divide(divisor, scale, roundingMode);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, and whose scale is {@code this.scale()}.  If
     * rounding must be performed to generate a result with the given
     * scale, the specified rounding mode is applied.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @param  roundingMode rounding mode to apply.
     * @return {@code this / divisor}
     * @throws ArithmeticException if {@code divisor==0}, or
     *         {@code roundingMode==RoundingMode.UNNECESSARY} and
     *         {@code this.scale()} is insufficient to represent the result
     *         of the division exactly.
     * @since 1.5
     */
    public BigDecimal divide(BigDecimal divisor, RoundingMode roundingMode) {
        return this.divide(divisor, scale, roundingMode.oldMode);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, and whose preferred scale is {@code (this.scale() -
     * divisor.scale())}; if the exact quotient cannot be
     * represented (because it has a non-terminating decimal
     * expansion) an {@code ArithmeticException} is thrown.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @throws ArithmeticException if the exact quotient does not have a
     *         terminating decimal expansion
     * @return {@code this / divisor}
     * @since 1.5
     * @author Joseph D. Darcy
     */
    public BigDecimal divide(BigDecimal divisor) {
        /*
         * Handle zero cases first.
         */
        if (divisor.signum() == 0) {   // x/0
            if (this.signum() == 0)    // 0/0
                throw new ArithmeticException("Division undefined");  // NaN
            throw new ArithmeticException("Division by zero");
        }

        // Calculate preferred scale
        int preferredScale = saturateLong((long) this.scale - divisor.scale);

        if (this.signum() == 0) // 0/y
            return zeroValueOf(preferredScale);
        else {
            /*
             * If the quotient this/divisor has a terminating decimal
             * expansion, the expansion can have no more than
             * (a.precision() + ceil(10*b.precision)/3) digits.
             * Therefore, create a MathContext object with this
             * precision and do a divide with the UNNECESSARY rounding
             * mode.
             */
            MathContext mc = new MathContext( (int)Math.min(this.precision() +
                                                            (long)Math.ceil(10.0*divisor.precision()/3.0),
                                                            Integer.MAX_VALUE),
                                              RoundingMode.UNNECESSARY);
            BigDecimal quotient;
            try {
                quotient = this.divide(divisor, mc);
            } catch (ArithmeticException e) {
                throw new ArithmeticException("Non-terminating decimal expansion; " +
                                              "no exact representable decimal result.");
            }

            int quotientScale = quotient.scale();

            // divide(BigDecimal, mc) tries to adjust the quotient to
            // the desired one by removing trailing zeros; since the
            // exact divide method does not have an explicit digit
            // limit, we can add zeros too.
            if (preferredScale > quotientScale)
                return quotient.setScale(preferredScale, ROUND_UNNECESSARY);

            return quotient;
        }
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this /
     * divisor)}, with rounding according to the context settings.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @param  mc the context to use.
     * @return {@code this / divisor}, rounded as necessary.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY} or
     *         {@code mc.precision == 0} and the quotient has a
     *         non-terminating decimal expansion.
     * @since  1.5
     */
    public BigDecimal divide(BigDecimal divisor, MathContext mc) {
        int mcp = mc.precision;
        if (mcp == 0)
            return divide(divisor);

        BigDecimal dividend = this;
        long preferredScale = (long)dividend.scale - divisor.scale;
        // Now calculate the answer.  We use the existing
        // divide-and-round method, but as this rounds to scale we have
        // to normalize the values here to achieve the desired result.
        // For x/y we first handle y=0 and x=0, and then normalize x and
        // y to give x' and y' with the following constraints:
        //   (a) 0.1 <= x' < 1
        //   (b)  x' <= y' < 10*x'
        // Dividing x'/y' with the required scale set to mc.precision then
        // will give a result in the range 0.1 to 1 rounded to exactly
        // the right number of digits (except in the case of a result of
        // 1.000... which can arise when x=y, or when rounding overflows
        // The 1.000... case will reduce properly to 1.
        if (divisor.signum() == 0) {      // x/0
            if (dividend.signum() == 0)    // 0/0
                throw new ArithmeticException("Division undefined");  // NaN
            throw new ArithmeticException("Division by zero");
        }
        if (dividend.signum() == 0) // 0/y
            return zeroValueOf(saturateLong(preferredScale));
        int xscale = dividend.precision();
        int yscale = divisor.precision();
        if(dividend.intCompact!=INFLATED) {
            if(divisor.intCompact!=INFLATED) {
                return divide(dividend.intCompact, xscale, divisor.intCompact, yscale, preferredScale, mc);
            } else {
                return divide(dividend.intCompact, xscale, divisor.intVal, yscale, preferredScale, mc);
            }
        } else {
            if(divisor.intCompact!=INFLATED) {
                return divide(dividend.intVal, xscale, divisor.intCompact, yscale, preferredScale, mc);
            } else {
                return divide(dividend.intVal, xscale, divisor.intVal, yscale, preferredScale, mc);
            }
        }
    }

    /**
     * Returns a {@code BigDecimal} whose value is the integer part
     * of the quotient {@code (this / divisor)} rounded down.  The
     * preferred scale of the result is {@code (this.scale() -
     * divisor.scale())}.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @return The integer part of {@code this / divisor}.
     * @throws ArithmeticException if {@code divisor==0}
     * @since  1.5
     */
    public BigDecimal divideToIntegralValue(BigDecimal divisor) {
        // Calculate preferred scale
        int preferredScale = saturateLong((long) this.scale - divisor.scale);
        if (this.compareMagnitude(divisor) < 0) {
            // much faster when this << divisor
            return zeroValueOf(preferredScale);
        }

        if (this.signum() == 0 && divisor.signum() != 0)
            return this.setScale(preferredScale, ROUND_UNNECESSARY);

        // Perform a divide with enough digits to round to a correct
        // integer value; then remove any fractional digits

        int maxDigits = (int)Math.min(this.precision() +
                                      (long)Math.ceil(10.0*divisor.precision()/3.0) +
                                      Math.abs((long)this.scale() - divisor.scale()) + 2,
                                      Integer.MAX_VALUE);
        BigDecimal quotient = this.divide(divisor, new MathContext(maxDigits,
                                                                   RoundingMode.DOWN));
        if (quotient.scale > 0) {
            quotient = quotient.setScale(0, RoundingMode.DOWN);
            quotient = stripZerosToMatchScale(quotient.intVal, quotient.intCompact, quotient.scale, preferredScale);
        }

        if (quotient.scale < preferredScale) {
            // pad with zeros if necessary
            quotient = quotient.setScale(preferredScale, ROUND_UNNECESSARY);
        }

        return quotient;
    }

    /**
     * Returns a {@code BigDecimal} whose value is the integer part
     * of {@code (this / divisor)}.  Since the integer part of the
     * exact quotient does not depend on the rounding mode, the
     * rounding mode does not affect the values returned by this
     * method.  The preferred scale of the result is
     * {@code (this.scale() - divisor.scale())}.  An
     * {@code ArithmeticException} is thrown if the integer part of
     * the exact quotient needs more than {@code mc.precision}
     * digits.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @param  mc the context to use.
     * @return The integer part of {@code this / divisor}.
     * @throws ArithmeticException if {@code divisor==0}
     * @throws ArithmeticException if {@code mc.precision} {@literal >} 0 and the result
     *         requires a precision of more than {@code mc.precision} digits.
     * @since  1.5
     * @author Joseph D. Darcy
     */
    public BigDecimal divideToIntegralValue(BigDecimal divisor, MathContext mc) {
        if (mc.precision == 0 || // exact result
            (this.compareMagnitude(divisor) < 0)) // zero result
            return divideToIntegralValue(divisor);

        // Calculate preferred scale
        int preferredScale = saturateLong((long)this.scale - divisor.scale);

        /*
         * Perform a normal divide to mc.precision digits.  If the
         * remainder has absolute value less than the divisor, the
         * integer portion of the quotient fits into mc.precision
         * digits.  Next, remove any fractional digits from the
         * quotient and adjust the scale to the preferred value.
         */
        BigDecimal result = this.divide(divisor, new MathContext(mc.precision, RoundingMode.DOWN));

        if (result.scale() < 0) {
            /*
             * Result is an integer. See if quotient represents the
             * full integer portion of the exact quotient; if it does,
             * the computed remainder will be less than the divisor.
             */
            BigDecimal product = result.multiply(divisor);
            // If the quotient is the full integer value,
            // |dividend-product| < |divisor|.
            if (this.subtract(product).compareMagnitude(divisor) >= 0) {
                throw new ArithmeticException("Division impossible");
            }
        } else if (result.scale() > 0) {
            /*
             * Integer portion of quotient will fit into precision
             * digits; recompute quotient to scale 0 to avoid double
             * rounding and then try to adjust, if necessary.
             */
            result = result.setScale(0, RoundingMode.DOWN);
        }
        // else result.scale() == 0;

        int precisionDiff;
        if ((preferredScale > result.scale()) &&
            (precisionDiff = mc.precision - result.precision()) > 0) {
            return result.setScale(result.scale() +
                                   Math.min(precisionDiff, preferredScale - result.scale) );
        } else {
            return stripZerosToMatchScale(result.intVal,result.intCompact,result.scale,preferredScale);
        }
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (this % divisor)}.
     *
     * <p>The remainder is given by
     * {@code this.subtract(this.divideToIntegralValue(divisor).multiply(divisor))}.
     * Note that this is not the modulo operation (the result can be
     * negative).
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @return {@code this % divisor}.
     * @throws ArithmeticException if {@code divisor==0}
     * @since  1.5
     */
    public BigDecimal remainder(BigDecimal divisor) {
        BigDecimal divrem[] = this.divideAndRemainder(divisor);
        return divrem[1];
    }


    /**
     * Returns a {@code BigDecimal} whose value is {@code (this %
     * divisor)}, with rounding according to the context settings.
     * The {@code MathContext} settings affect the implicit divide
     * used to compute the remainder.  The remainder computation
     * itself is by definition exact.  Therefore, the remainder may
     * contain more than {@code mc.getPrecision()} digits.
     *
     * <p>The remainder is given by
     * {@code this.subtract(this.divideToIntegralValue(divisor,
     * mc).multiply(divisor))}.  Note that this is not the modulo
     * operation (the result can be negative).
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided.
     * @param  mc the context to use.
     * @return {@code this % divisor}, rounded as necessary.
     * @throws ArithmeticException if {@code divisor==0}
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}, or {@code mc.precision}
     *         {@literal >} 0 and the result of {@code this.divideToIntgralValue(divisor)} would
     *         require a precision of more than {@code mc.precision} digits.
     * @see    #divideToIntegralValue(java.math.BigDecimal, java.math.MathContext)
     * @since  1.5
     */
    public BigDecimal remainder(BigDecimal divisor, MathContext mc) {
        BigDecimal divrem[] = this.divideAndRemainder(divisor, mc);
        return divrem[1];
    }

    /**
     * Returns a two-element {@code BigDecimal} array containing the
     * result of {@code divideToIntegralValue} followed by the result of
     * {@code remainder} on the two operands.
     *
     * <p>Note that if both the integer quotient and remainder are
     * needed, this method is faster than using the
     * {@code divideToIntegralValue} and {@code remainder} methods
     * separately because the division need only be carried out once.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided,
     *         and the remainder computed.
     * @return a two element {@code BigDecimal} array: the quotient
     *         (the result of {@code divideToIntegralValue}) is the initial element
     *         and the remainder is the final element.
     * @throws ArithmeticException if {@code divisor==0}
     * @see    #divideToIntegralValue(java.math.BigDecimal, java.math.MathContext)
     * @see    #remainder(java.math.BigDecimal, java.math.MathContext)
     * @since  1.5
     */
    public BigDecimal[] divideAndRemainder(BigDecimal divisor) {
        // we use the identity  x = i * y + r to determine r
        BigDecimal[] result = new BigDecimal[2];

        result[0] = this.divideToIntegralValue(divisor);
        result[1] = this.subtract(result[0].multiply(divisor));
        return result;
    }

    /**
     * Returns a two-element {@code BigDecimal} array containing the
     * result of {@code divideToIntegralValue} followed by the result of
     * {@code remainder} on the two operands calculated with rounding
     * according to the context settings.
     *
     * <p>Note that if both the integer quotient and remainder are
     * needed, this method is faster than using the
     * {@code divideToIntegralValue} and {@code remainder} methods
     * separately because the division need only be carried out once.
     *
     * @param  divisor value by which this {@code BigDecimal} is to be divided,
     *         and the remainder computed.
     * @param  mc the context to use.
     * @return a two element {@code BigDecimal} array: the quotient
     *         (the result of {@code divideToIntegralValue}) is the
     *         initial element and the remainder is the final element.
     * @throws ArithmeticException if {@code divisor==0}
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}, or {@code mc.precision}
     *         {@literal >} 0 and the result of {@code this.divideToIntgralValue(divisor)} would
     *         require a precision of more than {@code mc.precision} digits.
     * @see    #divideToIntegralValue(java.math.BigDecimal, java.math.MathContext)
     * @see    #remainder(java.math.BigDecimal, java.math.MathContext)
     * @since  1.5
     */
    public BigDecimal[] divideAndRemainder(BigDecimal divisor, MathContext mc) {
        if (mc.precision == 0)
            return divideAndRemainder(divisor);

        BigDecimal[] result = new BigDecimal[2];
        BigDecimal lhs = this;

        result[0] = lhs.divideToIntegralValue(divisor, mc);
        result[1] = lhs.subtract(result[0].multiply(divisor));
        return result;
    }

    /**
     * Returns a {@code BigDecimal} whose value is
     * <tt>(this<sup>n</sup>)</tt>, The power is computed exactly, to
     * unlimited precision.
     *
     * <p>The parameter {@code n} must be in the range 0 through
     * 999999999, inclusive.  {@code ZERO.pow(0)} returns {@link
     * #ONE}.
     *
     * Note that future releases may expand the allowable exponent
     * range of this method.
     *
     * @param  n power to raise this {@code BigDecimal} to.
     * @return <tt>this<sup>n</sup></tt>
     * @throws ArithmeticException if {@code n} is out of range.
     * @since  1.5
     */
    public BigDecimal pow(int n) {
        if (n < 0 || n > 999999999)
            throw new ArithmeticException("Invalid operation");
        // No need to calculate pow(n) if result will over/underflow.
        // Don't attempt to support "supernormal" numbers.
        int newScale = checkScale((long)scale * n);
        return new BigDecimal(this.inflated().pow(n), newScale);
    }


    /**
     * Returns a {@code BigDecimal} whose value is
     * <tt>(this<sup>n</sup>)</tt>.  The current implementation uses
     * the core algorithm defined in ANSI standard X3.274-1996 with
     * rounding according to the context settings.  In general, the
     * returned numerical value is within two ulps of the exact
     * numerical value for the chosen precision.  Note that future
     * releases may use a different algorithm with a decreased
     * allowable error bound and increased allowable exponent range.
     *
     * <p>The X3.274-1996 algorithm is:
     *
     * <ul>
     * <li> An {@code ArithmeticException} exception is thrown if
     *  <ul>
     *    <li>{@code abs(n) > 999999999}
     *    <li>{@code mc.precision == 0} and {@code n < 0}
     *    <li>{@code mc.precision > 0} and {@code n} has more than
     *    {@code mc.precision} decimal digits
     *  </ul>
     *
     * <li> if {@code n} is zero, {@link #ONE} is returned even if
     * {@code this} is zero, otherwise
     * <ul>
     *   <li> if {@code n} is positive, the result is calculated via
     *   the repeated squaring technique into a single accumulator.
     *   The individual multiplications with the accumulator use the
     *   same math context settings as in {@code mc} except for a
     *   precision increased to {@code mc.precision + elength + 1}
     *   where {@code elength} is the number of decimal digits in
     *   {@code n}.
     *
     *   <li> if {@code n} is negative, the result is calculated as if
     *   {@code n} were positive; this value is then divided into one
     *   using the working precision specified above.
     *
     *   <li> The final value from either the positive or negative case
     *   is then rounded to the destination precision.
     *   </ul>
     * </ul>
     *
     * @param  n power to raise this {@code BigDecimal} to.
     * @param  mc the context to use.
     * @return <tt>this<sup>n</sup></tt> using the ANSI standard X3.274-1996
     *         algorithm
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}, or {@code n} is out
     *         of range.
     * @since  1.5
     */
    public BigDecimal pow(int n, MathContext mc) {
        if (mc.precision == 0)
            return pow(n);
        if (n < -999999999 || n > 999999999)
            throw new ArithmeticException("Invalid operation");
        if (n == 0)
            return ONE;                      // x**0 == 1 in X3.274
        BigDecimal lhs = this;
        MathContext workmc = mc;           // working settings
        int mag = Math.abs(n);               // magnitude of n
        if (mc.precision > 0) {
            int elength = longDigitLength(mag); // length of n in digits
            if (elength > mc.precision)        // X3.274 rule
                throw new ArithmeticException("Invalid operation");
            workmc = new MathContext(mc.precision + elength + 1,
                                      mc.roundingMode);
        }
        // ready to carry out power calculation...
        BigDecimal acc = ONE;           // accumulator
        boolean seenbit = false;        // set once we've seen a 1-bit
        for (int i=1;;i++) {            // for each bit [top bit ignored]
            mag += mag;                 // shift left 1 bit
            if (mag < 0) {              // top bit is set
                seenbit = true;         // OK, we're off
                acc = acc.multiply(lhs, workmc); // acc=acc*x
            }
            if (i == 31)
                break;                  // that was the last bit
            if (seenbit)
                acc=acc.multiply(acc, workmc);   // acc=acc*acc [square]
                // else (!seenbit) no point in squaring ONE
        }
        // if negative n, calculate the reciprocal using working precision
        if (n < 0) // [hence mc.precision>0]
            acc=ONE.divide(acc, workmc);
        // round to final precision and strip zeros
        return doRound(acc, mc);
    }

    /**
     * Returns a {@code BigDecimal} whose value is the absolute value
     * of this {@code BigDecimal}, and whose scale is
     * {@code this.scale()}.
     *
     * @return {@code abs(this)}
     */
    public BigDecimal abs() {
        return (signum() < 0 ? negate() : this);
    }

    /**
     * Returns a {@code BigDecimal} whose value is the absolute value
     * of this {@code BigDecimal}, with rounding according to the
     * context settings.
     *
     * @param mc the context to use.
     * @return {@code abs(this)}, rounded as necessary.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since 1.5
     */
    public BigDecimal abs(MathContext mc) {
        return (signum() < 0 ? negate(mc) : plus(mc));
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (-this)},
     * and whose scale is {@code this.scale()}.
     *
     * @return {@code -this}.
     */
    public BigDecimal negate() {
        if (intCompact == INFLATED) {
            return new BigDecimal(intVal.negate(), INFLATED, scale, precision);
        } else {
            return valueOf(-intCompact, scale, precision);
        }
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (-this)},
     * with rounding according to the context settings.
     *
     * @param mc the context to use.
     * @return {@code -this}, rounded as necessary.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @since  1.5
     */
    public BigDecimal negate(MathContext mc) {
        return negate().plus(mc);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (+this)}, and whose
     * scale is {@code this.scale()}.
     *
     * <p>This method, which simply returns this {@code BigDecimal}
     * is included for symmetry with the unary minus method {@link
     * #negate()}.
     *
     * @return {@code this}.
     * @see #negate()
     * @since  1.5
     */
    public BigDecimal plus() {
        return this;
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (+this)},
     * with rounding according to the context settings.
     *
     * <p>The effect of this method is identical to that of the {@link
     * #round(MathContext)} method.
     *
     * @param mc the context to use.
     * @return {@code this}, rounded as necessary.  A zero result will
     *         have a scale of 0.
     * @throws ArithmeticException if the result is inexact but the
     *         rounding mode is {@code UNNECESSARY}.
     * @see    #round(MathContext)
     * @since  1.5
     */
    public BigDecimal plus(MathContext mc) {
        if (mc.precision == 0)                 // no rounding please
            return this;
        return doRound(this, mc);
    }

    /**
     * Returns the signum function of this {@code BigDecimal}.
     *
     * @return -1, 0, or 1 as the value of this {@code BigDecimal}
     *         is negative, zero, or positive.
     */
    public int signum() {
        return (intCompact != INFLATED)?
            Long.signum(intCompact):
            intVal.signum();
    }

    /**
     * Returns the <i>scale</i> of this {@code BigDecimal}.  If zero
     * or positive, the scale is the number of digits to the right of
     * the decimal point.  If negative, the unscaled value of the
     * number is multiplied by ten to the power of the negation of the
     * scale.  For example, a scale of {@code -3} means the unscaled
     * value is multiplied by 1000.
     *
     * @return the scale of this {@code BigDecimal}.
     */
    public int scale() {
        return scale;
    }

    /**
     * Returns the <i>precision</i> of this {@code BigDecimal}.  (The
     * precision is the number of digits in the unscaled value.)
     *
     * <p>The precision of a zero value is 1.
     *
     * @return the precision of this {@code BigDecimal}.
     * @since  1.5
     */
    public int precision() {
        int result = precision;
        if (result == 0) {
            long s = intCompact;
            if (s != INFLATED)
                result = longDigitLength(s);
            else
                result = bigDigitLength(intVal);
            precision = result;
        }
        return result;
    }


    /**
     * Returns a {@code BigInteger} whose value is the <i>unscaled
     * value</i> of this {@code BigDecimal}.  (Computes <tt>(this *
     * 10<sup>this.scale()</sup>)</tt>.)
     *
     * @return the unscaled value of this {@code BigDecimal}.
     * @since  1.2
     */
    public BigInteger unscaledValue() {
        return this.inflated();
    }

    // Rounding Modes

    /**
     * Rounding mode to round away from zero.  Always increments the
     * digit prior to a nonzero discarded fraction.  Note that this rounding
     * mode never decreases the magnitude of the calculated value.
     */
    public final static int ROUND_UP =           0;

    /**
     * Rounding mode to round towards zero.  Never increments the digit
     * prior to a discarded fraction (i.e., truncates).  Note that this
     * rounding mode never increases the magnitude of the calculated value.
     */
    public final static int ROUND_DOWN =         1;

    /**
     * Rounding mode to round towards positive infinity.  If the
     * {@code BigDecimal} is positive, behaves as for
     * {@code ROUND_UP}; if negative, behaves as for
     * {@code ROUND_DOWN}.  Note that this rounding mode never
     * decreases the calculated value.
     */
    public final static int ROUND_CEILING =      2;

    /**
     * Rounding mode to round towards negative infinity.  If the
     * {@code BigDecimal} is positive, behave as for
     * {@code ROUND_DOWN}; if negative, behave as for
     * {@code ROUND_UP}.  Note that this rounding mode never
     * increases the calculated value.
     */
    public final static int ROUND_FLOOR =        3;

    /**
     * Rounding mode to round towards {@literal "nearest neighbor"}
     * unless both neighbors are equidistant, in which case round up.
     * Behaves as for {@code ROUND_UP} if the discarded fraction is
     * &ge; 0.5; otherwise, behaves as for {@code ROUND_DOWN}.  Note
     * that this is the rounding mode that most of us were taught in
     * grade school.
     */
    public final static int ROUND_HALF_UP =      4;

    /**
     * Rounding mode to round towards {@literal "nearest neighbor"}
     * unless both neighbors are equidistant, in which case round
     * down.  Behaves as for {@code ROUND_UP} if the discarded
     * fraction is {@literal >} 0.5; otherwise, behaves as for
     * {@code ROUND_DOWN}.
     */
    public final static int ROUND_HALF_DOWN =    5;

    /**
     * Rounding mode to round towards the {@literal "nearest neighbor"}
     * unless both neighbors are equidistant, in which case, round
     * towards the even neighbor.  Behaves as for
     * {@code ROUND_HALF_UP} if the digit to the left of the
     * discarded fraction is odd; behaves as for
     * {@code ROUND_HALF_DOWN} if it's even.  Note that this is the
     * rounding mode that minimizes cumulative error when applied
     * repeatedly over a sequence of calculations.
     */
    public final static int ROUND_HALF_EVEN =    6;

    /**
     * Rounding mode to assert that the requested operation has an exact
     * result, hence no rounding is necessary.  If this rounding mode is
     * specified on an operation that yields an inexact result, an
     * {@code ArithmeticException} is thrown.
     */
    public final static int ROUND_UNNECESSARY =  7;


    // Scaling/Rounding Operations

    /**
     * Returns a {@code BigDecimal} rounded according to the
     * {@code MathContext} settings.  If the precision setting is 0 then
     * no rounding takes place.
     *
     * <p>The effect of this method is identical to that of the
     * {@link #plus(MathContext)} method.
     *
     * @param mc the context to use.
     * @return a {@code BigDecimal} rounded according to the
     *         {@code MathContext} settings.
     * @throws ArithmeticException if the rounding mode is
     *         {@code UNNECESSARY} and the
     *         {@code BigDecimal}  operation would require rounding.
     * @see    #plus(MathContext)
     * @since  1.5
     */
    public BigDecimal round(MathContext mc) {
        return plus(mc);
    }

    /**
     * Returns a {@code BigDecimal} whose scale is the specified
     * value, and whose unscaled value is determined by multiplying or
     * dividing this {@code BigDecimal}'s unscaled value by the
     * appropriate power of ten to maintain its overall value.  If the
     * scale is reduced by the operation, the unscaled value must be
     * divided (rather than multiplied), and the value may be changed;
     * in this case, the specified rounding mode is applied to the
     * division.
     *
     * <p>Note that since BigDecimal objects are immutable, calls of
     * this method do <i>not</i> result in the original object being
     * modified, contrary to the usual convention of having methods
     * named <tt>set<i>X</i></tt> mutate field <i>{@code X}</i>.
     * Instead, {@code setScale} returns an object with the proper
     * scale; the returned object may or may not be newly allocated.
     *
     * @param  newScale scale of the {@code BigDecimal} value to be returned.
     * @param  roundingMode The rounding mode to apply.
     * @return a {@code BigDecimal} whose scale is the specified value,
     *         and whose unscaled value is determined by multiplying or
     *         dividing this {@code BigDecimal}'s unscaled value by the
     *         appropriate power of ten to maintain its overall value.
     * @throws ArithmeticException if {@code roundingMode==UNNECESSARY}
     *         and the specified scaling operation would require
     *         rounding.
     * @see    RoundingMode
     * @since  1.5
     */
    public BigDecimal setScale(int newScale, RoundingMode roundingMode) {
        return setScale(newScale, roundingMode.oldMode);
    }

    /**
     * Returns a {@code BigDecimal} whose scale is the specified
     * value, and whose unscaled value is determined by multiplying or
     * dividing this {@code BigDecimal}'s unscaled value by the
     * appropriate power of ten to maintain its overall value.  If the
     * scale is reduced by the operation, the unscaled value must be
     * divided (rather than multiplied), and the value may be changed;
     * in this case, the specified rounding mode is applied to the
     * division.
     *
     * <p>Note that since BigDecimal objects are immutable, calls of
     * this method do <i>not</i> result in the original object being
     * modified, contrary to the usual convention of having methods
     * named <tt>set<i>X</i></tt> mutate field <i>{@code X}</i>.
     * Instead, {@code setScale} returns an object with the proper
     * scale; the returned object may or may not be newly allocated.
     *
     * <p>The new {@link #setScale(int, RoundingMode)} method should
     * be used in preference to this legacy method.
     *
     * @param  newScale scale of the {@code BigDecimal} value to be returned.
     * @param  roundingMode The rounding mode to apply.
     * @return a {@code BigDecimal} whose scale is the specified value,
     *         and whose unscaled value is determined by multiplying or
     *         dividing this {@code BigDecimal}'s unscaled value by the
     *         appropriate power of ten to maintain its overall value.
     * @throws ArithmeticException if {@code roundingMode==ROUND_UNNECESSARY}
     *         and the specified scaling operation would require
     *         rounding.
     * @throws IllegalArgumentException if {@code roundingMode} does not
     *         represent a valid rounding mode.
     * @see    #ROUND_UP
     * @see    #ROUND_DOWN
     * @see    #ROUND_CEILING
     * @see    #ROUND_FLOOR
     * @see    #ROUND_HALF_UP
     * @see    #ROUND_HALF_DOWN
     * @see    #ROUND_HALF_EVEN
     * @see    #ROUND_UNNECESSARY
     */
    public BigDecimal setScale(int newScale, int roundingMode) {
        if (roundingMode < ROUND_UP || roundingMode > ROUND_UNNECESSARY)
            throw new IllegalArgumentException("Invalid rounding mode");

        int oldScale = this.scale;
        if (newScale == oldScale)        // easy case
            return this;
        if (this.signum() == 0)            // zero can have any scale
            return zeroValueOf(newScale);
        if(this.intCompact!=INFLATED) {
            long rs = this.intCompact;
            if (newScale > oldScale) {
                int raise = checkScale((long) newScale - oldScale);
                if ((rs = longMultiplyPowerTen(rs, raise)) != INFLATED) {
                    return valueOf(rs,newScale);
                }
                BigInteger rb = bigMultiplyPowerTen(raise);
                return new BigDecimal(rb, INFLATED, newScale, (precision > 0) ? precision + raise : 0);
            } else {
                // newScale < oldScale -- drop some digits
                // Can't predict the precision due to the effect of rounding.
                int drop = checkScale((long) oldScale - newScale);
                if (drop < LONG_TEN_POWERS_TABLE.length) {
                    return divideAndRound(rs, LONG_TEN_POWERS_TABLE[drop], newScale, roundingMode, newScale);
                } else {
                    return divideAndRound(this.inflated(), bigTenToThe(drop), newScale, roundingMode, newScale);
                }
            }
        } else {
            if (newScale > oldScale) {
                int raise = checkScale((long) newScale - oldScale);
                BigInteger rb = bigMultiplyPowerTen(this.intVal,raise);
                return new BigDecimal(rb, INFLATED, newScale, (precision > 0) ? precision + raise : 0);
            } else {
                // newScale < oldScale -- drop some digits
                // Can't predict the precision due to the effect of rounding.
                int drop = checkScale((long) oldScale - newScale);
                if (drop < LONG_TEN_POWERS_TABLE.length)
                    return divideAndRound(this.intVal, LONG_TEN_POWERS_TABLE[drop], newScale, roundingMode,
                                          newScale);
                else
                    return divideAndRound(this.intVal,  bigTenToThe(drop), newScale, roundingMode, newScale);
            }
        }
    }

    /**
     * Returns a {@code BigDecimal} whose scale is the specified
     * value, and whose value is numerically equal to this
     * {@code BigDecimal}'s.  Throws an {@code ArithmeticException}
     * if this is not possible.
     *
     * <p>This call is typically used to increase the scale, in which
     * case it is guaranteed that there exists a {@code BigDecimal}
     * of the specified scale and the correct value.  The call can
     * also be used to reduce the scale if the caller knows that the
     * {@code BigDecimal} has sufficiently many zeros at the end of
     * its fractional part (i.e., factors of ten in its integer value)
     * to allow for the rescaling without changing its value.
     *
     * <p>This method returns the same result as the two-argument
     * versions of {@code setScale}, but saves the caller the trouble
     * of specifying a rounding mode in cases where it is irrelevant.
     *
     * <p>Note that since {@code BigDecimal} objects are immutable,
     * calls of this method do <i>not</i> result in the original
     * object being modified, contrary to the usual convention of
     * having methods named <tt>set<i>X</i></tt> mutate field
     * <i>{@code X}</i>.  Instead, {@code setScale} returns an
     * object with the proper scale; the returned object may or may
     * not be newly allocated.
     *
     * @param  newScale scale of the {@code BigDecimal} value to be returned.
     * @return a {@code BigDecimal} whose scale is the specified value, and
     *         whose unscaled value is determined by multiplying or dividing
     *         this {@code BigDecimal}'s unscaled value by the appropriate
     *         power of ten to maintain its overall value.
     * @throws ArithmeticException if the specified scaling operation would
     *         require rounding.
     * @see    #setScale(int, int)
     * @see    #setScale(int, RoundingMode)
     */
    public BigDecimal setScale(int newScale) {
        return setScale(newScale, ROUND_UNNECESSARY);
    }

    // Decimal Point Motion Operations

    /**
     * Returns a {@code BigDecimal} which is equivalent to this one
     * with the decimal point moved {@code n} places to the left.  If
     * {@code n} is non-negative, the call merely adds {@code n} to
     * the scale.  If {@code n} is negative, the call is equivalent
     * to {@code movePointRight(-n)}.  The {@code BigDecimal}
     * returned by this call has value <tt>(this &times;
     * 10<sup>-n</sup>)</tt> and scale {@code max(this.scale()+n,
     * 0)}.
     *
     * @param  n number of places to move the decimal point to the left.
     * @return a {@code BigDecimal} which is equivalent to this one with the
     *         decimal point moved {@code n} places to the left.
     * @throws ArithmeticException if scale overflows.
     */
    public BigDecimal movePointLeft(int n) {
        // Cannot use movePointRight(-n) in case of n==Integer.MIN_VALUE
        int newScale = checkScale((long)scale + n);
        BigDecimal num = new BigDecimal(intVal, intCompact, newScale, 0);
        return num.scale < 0 ? num.setScale(0, ROUND_UNNECESSARY) : num;
    }

    /**
     * Returns a {@code BigDecimal} which is equivalent to this one
     * with the decimal point moved {@code n} places to the right.
     * If {@code n} is non-negative, the call merely subtracts
     * {@code n} from the scale.  If {@code n} is negative, the call
     * is equivalent to {@code movePointLeft(-n)}.  The
     * {@code BigDecimal} returned by this call has value <tt>(this
     * &times; 10<sup>n</sup>)</tt> and scale {@code max(this.scale()-n,
     * 0)}.
     *
     * @param  n number of places to move the decimal point to the right.
     * @return a {@code BigDecimal} which is equivalent to this one
     *         with the decimal point moved {@code n} places to the right.
     * @throws ArithmeticException if scale overflows.
     */
    public BigDecimal movePointRight(int n) {
        // Cannot use movePointLeft(-n) in case of n==Integer.MIN_VALUE
        int newScale = checkScale((long)scale - n);
        BigDecimal num = new BigDecimal(intVal, intCompact, newScale, 0);
        return num.scale < 0 ? num.setScale(0, ROUND_UNNECESSARY) : num;
    }

    /**
     * Returns a BigDecimal whose numerical value is equal to
     * ({@code this} * 10<sup>n</sup>).  The scale of
     * the result is {@code (this.scale() - n)}.
     *
     * @param n the exponent power of ten to scale by
     * @return a BigDecimal whose numerical value is equal to
     * ({@code this} * 10<sup>n</sup>)
     * @throws ArithmeticException if the scale would be
     *         outside the range of a 32-bit integer.
     *
     * @since 1.5
     */
    public BigDecimal scaleByPowerOfTen(int n) {
        return new BigDecimal(intVal, intCompact,
                              checkScale((long)scale - n), precision);
    }

    /**
     * Returns a {@code BigDecimal} which is numerically equal to
     * this one but with any trailing zeros removed from the
     * representation.  For example, stripping the trailing zeros from
     * the {@code BigDecimal} value {@code 600.0}, which has
     * [{@code BigInteger}, {@code scale}] components equals to
     * [6000, 1], yields {@code 6E2} with [{@code BigInteger},
     * {@code scale}] components equals to [6, -2].  If
     * this BigDecimal is numerically equal to zero, then
     * {@code BigDecimal.ZERO} is returned.
     *
     * @return a numerically equal {@code BigDecimal} with any
     * trailing zeros removed.
     * @since 1.5
     */
    public BigDecimal stripTrailingZeros() {
        if (intCompact == 0 || (intVal != null && intVal.signum() == 0)) {
            return BigDecimal.ZERO;
        } else if (intCompact != INFLATED) {
            return createAndStripZerosToMatchScale(intCompact, scale, Long.MIN_VALUE);
        } else {
            return createAndStripZerosToMatchScale(intVal, scale, Long.MIN_VALUE);
        }
    }

    // Comparison Operations

    /**
     * Compares this {@code BigDecimal} with the specified
     * {@code BigDecimal}.  Two {@code BigDecimal} objects that are
     * equal in value but have a different scale (like 2.0 and 2.00)
     * are considered equal by this method.  This method is provided
     * in preference to individual methods for each of the six boolean
     * comparison operators ({@literal <}, ==,
     * {@literal >}, {@literal >=}, !=, {@literal <=}).  The
     * suggested idiom for performing these comparisons is:
     * {@code (x.compareTo(y)} &lt;<i>op</i>&gt; {@code 0)}, where
     * &lt;<i>op</i>&gt; is one of the six comparison operators.
     *
     * @param  val {@code BigDecimal} to which this {@code BigDecimal} is
     *         to be compared.
     * @return -1, 0, or 1 as this {@code BigDecimal} is numerically
     *          less than, equal to, or greater than {@code val}.
     */
    public int compareTo(BigDecimal val) {
        // Quick path for equal scale and non-inflated case.
        if (scale == val.scale) {
            long xs = intCompact;
            long ys = val.intCompact;
            if (xs != INFLATED && ys != INFLATED)
                return xs != ys ? ((xs > ys) ? 1 : -1) : 0;
        }
        int xsign = this.signum();
        int ysign = val.signum();
        if (xsign != ysign)
            return (xsign > ysign) ? 1 : -1;
        if (xsign == 0)
            return 0;
        int cmp = compareMagnitude(val);
        return (xsign > 0) ? cmp : -cmp;
    }

    /**
     * Version of compareTo that ignores sign.
     */
    private int compareMagnitude(BigDecimal val) {
        // Match scales, avoid unnecessary inflation
        long ys = val.intCompact;
        long xs = this.intCompact;
        if (xs == 0)
            return (ys == 0) ? 0 : -1;
        if (ys == 0)
            return 1;

        long sdiff = (long)this.scale - val.scale;
        if (sdiff != 0) {
            // Avoid matching scales if the (adjusted) exponents differ
            long xae = (long)this.precision() - this.scale;   // [-1]
            long yae = (long)val.precision() - val.scale;     // [-1]
            if (xae < yae)
                return -1;
            if (xae > yae)
                return 1;
            BigInteger rb = null;
            if (sdiff < 0) {
                // The cases sdiff <= Integer.MIN_VALUE intentionally fall through.
                if ( sdiff > Integer.MIN_VALUE &&
                      (xs == INFLATED ||
                      (xs = longMultiplyPowerTen(xs, (int)-sdiff)) == INFLATED) &&
                     ys == INFLATED) {
                    rb = bigMultiplyPowerTen((int)-sdiff);
                    return rb.compareMagnitude(val.intVal);
                }
            } else { // sdiff > 0
                // The cases sdiff > Integer.MAX_VALUE intentionally fall through.
                if ( sdiff <= Integer.MAX_VALUE &&
                      (ys == INFLATED ||
                      (ys = longMultiplyPowerTen(ys, (int)sdiff)) == INFLATED) &&
                     xs == INFLATED) {
                    rb = val.bigMultiplyPowerTen((int)sdiff);
                    return this.intVal.compareMagnitude(rb);
                }
            }
        }
        if (xs != INFLATED)
            return (ys != INFLATED) ? longCompareMagnitude(xs, ys) : -1;
        else if (ys != INFLATED)
            return 1;
        else
            return this.intVal.compareMagnitude(val.intVal);
    }

    /**
     * Compares this {@code BigDecimal} with the specified
     * {@code Object} for equality.  Unlike {@link
     * #compareTo(BigDecimal) compareTo}, this method considers two
     * {@code BigDecimal} objects equal only if they are equal in
     * value and scale (thus 2.0 is not equal to 2.00 when compared by
     * this method).
     *
     * @param  x {@code Object} to which this {@code BigDecimal} is
     *         to be compared.
     * @return {@code true} if and only if the specified {@code Object} is a
     *         {@code BigDecimal} whose value and scale are equal to this
     *         {@code BigDecimal}'s.
     * @see    #compareTo(java.math.BigDecimal)
     * @see    #hashCode
     */
    @Override
    public boolean equals(Object x) {
        if (!(x instanceof BigDecimal))
            return false;
        BigDecimal xDec = (BigDecimal) x;
        if (x == this)
            return true;
        if (scale != xDec.scale)
            return false;
        long s = this.intCompact;
        long xs = xDec.intCompact;
        if (s != INFLATED) {
            if (xs == INFLATED)
                xs = compactValFor(xDec.intVal);
            return xs == s;
        } else if (xs != INFLATED)
            return xs == compactValFor(this.intVal);

        return this.inflated().equals(xDec.inflated());
    }

    /**
     * Returns the minimum of this {@code BigDecimal} and
     * {@code val}.
     *
     * @param  val value with which the minimum is to be computed.
     * @return the {@code BigDecimal} whose value is the lesser of this
     *         {@code BigDecimal} and {@code val}.  If they are equal,
     *         as defined by the {@link #compareTo(BigDecimal) compareTo}
     *         method, {@code this} is returned.
     * @see    #compareTo(java.math.BigDecimal)
     */
    public BigDecimal min(BigDecimal val) {
        return (compareTo(val) <= 0 ? this : val);
    }

    /**
     * Returns the maximum of this {@code BigDecimal} and {@code val}.
     *
     * @param  val value with which the maximum is to be computed.
     * @return the {@code BigDecimal} whose value is the greater of this
     *         {@code BigDecimal} and {@code val}.  If they are equal,
     *         as defined by the {@link #compareTo(BigDecimal) compareTo}
     *         method, {@code this} is returned.
     * @see    #compareTo(java.math.BigDecimal)
     */
    public BigDecimal max(BigDecimal val) {
        return (compareTo(val) >= 0 ? this : val);
    }

    // Hash Function

    /**
     * Returns the hash code for this {@code BigDecimal}.  Note that
     * two {@code BigDecimal} objects that are numerically equal but
     * differ in scale (like 2.0 and 2.00) will generally <i>not</i>
     * have the same hash code.
     *
     * @return hash code for this {@code BigDecimal}.
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        if (intCompact != INFLATED) {
            long val2 = (intCompact < 0)? -intCompact : intCompact;
            int temp = (int)( ((int)(val2 >>> 32)) * 31  +
                              (val2 & LONG_MASK));
            return 31*((intCompact < 0) ?-temp:temp) + scale;
        } else
            return 31*intVal.hashCode() + scale;
    }

    // Format Converters

    /**
     * Returns the string representation of this {@code BigDecimal},
     * using scientific notation if an exponent is needed.
     *
     * <p>A standard canonical string form of the {@code BigDecimal}
     * is created as though by the following steps: first, the
     * absolute value of the unscaled value of the {@code BigDecimal}
     * is converted to a string in base ten using the characters
     * {@code '0'} through {@code '9'} with no leading zeros (except
     * if its value is zero, in which case a single {@code '0'}
     * character is used).
     *
     * <p>Next, an <i>adjusted exponent</i> is calculated; this is the
     * negated scale, plus the number of characters in the converted
     * unscaled value, less one.  That is,
     * {@code -scale+(ulength-1)}, where {@code ulength} is the
     * length of the absolute value of the unscaled value in decimal
     * digits (its <i>precision</i>).
     *
     * <p>If the scale is greater than or equal to zero and the
     * adjusted exponent is greater than or equal to {@code -6}, the
     * number will be converted to a character form without using
     * exponential notation.  In this case, if the scale is zero then
     * no decimal point is added and if the scale is positive a
     * decimal point will be inserted with the scale specifying the
     * number of characters to the right of the decimal point.
     * {@code '0'} characters are added to the left of the converted
     * unscaled value as necessary.  If no character precedes the
     * decimal point after this insertion then a conventional
     * {@code '0'} character is prefixed.
     *
     * <p>Otherwise (that is, if the scale is negative, or the
     * adjusted exponent is less than {@code -6}), the number will be
     * converted to a character form using exponential notation.  In
     * this case, if the converted {@code BigInteger} has more than
     * one digit a decimal point is inserted after the first digit.
     * An exponent in character form is then suffixed to the converted
     * unscaled value (perhaps with inserted decimal point); this
     * comprises the letter {@code 'E'} followed immediately by the
     * adjusted exponent converted to a character form.  The latter is
     * in base ten, using the characters {@code '0'} through
     * {@code '9'} with no leading zeros, and is always prefixed by a
     * sign character {@code '-'} (<tt>'&#92;u002D'</tt>) if the
     * adjusted exponent is negative, {@code '+'}
     * (<tt>'&#92;u002B'</tt>) otherwise).
     *
     * <p>Finally, the entire string is prefixed by a minus sign
     * character {@code '-'} (<tt>'&#92;u002D'</tt>) if the unscaled
     * value is less than zero.  No sign character is prefixed if the
     * unscaled value is zero or positive.
     *
     * <p><b>Examples:</b>
     * <p>For each representation [<i>unscaled value</i>, <i>scale</i>]
     * on the left, the resulting string is shown on the right.
     * <pre>
     * [123,0]      "123"
     * [-123,0]     "-123"
     * [123,-1]     "1.23E+3"
     * [123,-3]     "1.23E+5"
     * [123,1]      "12.3"
     * [123,5]      "0.00123"
     * [123,10]     "1.23E-8"
     * [-123,12]    "-1.23E-10"
     * </pre>
     *
     * <b>Notes:</b>
     * <ol>
     *
     * <li>There is a one-to-one mapping between the distinguishable
     * {@code BigDecimal} values and the result of this conversion.
     * That is, every distinguishable {@code BigDecimal} value
     * (unscaled value and scale) has a unique string representation
     * as a result of using {@code toString}.  If that string
     * representation is converted back to a {@code BigDecimal} using
     * the {@link #BigDecimal(String)} constructor, then the original
     * value will be recovered.
     *
     * <li>The string produced for a given number is always the same;
     * it is not affected by locale.  This means that it can be used
     * as a canonical string representation for exchanging decimal
     * data, or as a key for a Hashtable, etc.  Locale-sensitive
     * number formatting and parsing is handled by the {@link
     * java.text.NumberFormat} class and its subclasses.
     *
     * <li>The {@link #toEngineeringString} method may be used for
     * presenting numbers with exponents in engineering notation, and the
     * {@link #setScale(int,RoundingMode) setScale} method may be used for
     * rounding a {@code BigDecimal} so it has a known number of digits after
     * the decimal point.
     *
     * <li>The digit-to-character mapping provided by
     * {@code Character.forDigit} is used.
     *
     * </ol>
     *
     * @return string representation of this {@code BigDecimal}.
     * @see    Character#forDigit
     * @see    #BigDecimal(java.lang.String)
     */
    @Override
    public String toString() {
        String sc = stringCache;
        if (sc == null)
            stringCache = sc = layoutChars(true);
        return sc;
    }

    /**
     * Returns a string representation of this {@code BigDecimal},
     * using engineering notation if an exponent is needed.
     *
     * <p>Returns a string that represents the {@code BigDecimal} as
     * described in the {@link #toString()} method, except that if
     * exponential notation is used, the power of ten is adjusted to
     * be a multiple of three (engineering notation) such that the
     * integer part of nonzero values will be in the range 1 through
     * 999.  If exponential notation is used for zero values, a
     * decimal point and one or two fractional zero digits are used so
     * that the scale of the zero value is preserved.  Note that
     * unlike the output of {@link #toString()}, the output of this
     * method is <em>not</em> guaranteed to recover the same [integer,
     * scale] pair of this {@code BigDecimal} if the output string is
     * converting back to a {@code BigDecimal} using the {@linkplain
     * #BigDecimal(String) string constructor}.  The result of this method meets
     * the weaker constraint of always producing a numerically equal
     * result from applying the string constructor to the method's output.
     *
     * @return string representation of this {@code BigDecimal}, using
     *         engineering notation if an exponent is needed.
     * @since  1.5
     */
    public String toEngineeringString() {
        return layoutChars(false);
    }

    /**
     * Returns a string representation of this {@code BigDecimal}
     * without an exponent field.  For values with a positive scale,
     * the number of digits to the right of the decimal point is used
     * to indicate scale.  For values with a zero or negative scale,
     * the resulting string is generated as if the value were
     * converted to a numerically equal value with zero scale and as
     * if all the trailing zeros of the zero scale value were present
     * in the result.
     *
     * The entire string is prefixed by a minus sign character '-'
     * (<tt>'&#92;u002D'</tt>) if the unscaled value is less than
     * zero. No sign character is prefixed if the unscaled value is
     * zero or positive.
     *
     * Note that if the result of this method is passed to the
     * {@linkplain #BigDecimal(String) string constructor}, only the
     * numerical value of this {@code BigDecimal} will necessarily be
     * recovered; the representation of the new {@code BigDecimal}
     * may have a different scale.  In particular, if this
     * {@code BigDecimal} has a negative scale, the string resulting
     * from this method will have a scale of zero when processed by
     * the string constructor.
     *
     * (This method behaves analogously to the {@code toString}
     * method in 1.4 and earlier releases.)
     *
     * @return a string representation of this {@code BigDecimal}
     * without an exponent field.
     * @since 1.5
     * @see #toString()
     * @see #toEngineeringString()
     */
    public String toPlainString() {
        if(scale==0) {
            if(intCompact!=INFLATED) {
                return Long.toString(intCompact);
            } else {
                return intVal.toString();
            }
        }
        if(this.scale<0) { // No decimal point
            if(signum()==0) {
                return "0";
            }
            int tailingZeros = checkScaleNonZero((-(long)scale));
            StringBuilder buf;
            if(intCompact!=INFLATED) {
                buf = new StringBuilder(20+tailingZeros);
                buf.append(intCompact);
            } else {
                String str = intVal.toString();
                buf = new StringBuilder(str.length()+tailingZeros);
                buf.append(str);
            }
            for (int i = 0; i < tailingZeros; i++)
                buf.append('0');
            return buf.toString();
        }
        String str ;
        if(intCompact!=INFLATED) {
            str = Long.toString(Math.abs(intCompact));
        } else {
            str = intVal.abs().toString();
        }
        return getValueString(signum(), str, scale);
    }

    /* Returns a digit.digit string */
    private String getValueString(int signum, String intString, int scale) {
        /* Insert decimal point */
        StringBuilder buf;
        int insertionPoint = intString.length() - scale;
        if (insertionPoint == 0) {  /* Point goes right before intVal */
            return (signum<0 ? "-0." : "0.") + intString;
        } else if (insertionPoint > 0) { /* Point goes inside intVal */
            buf = new StringBuilder(intString);
            buf.insert(insertionPoint, '.');
            if (signum < 0)
                buf.insert(0, '-');
        } else { /* We must insert zeros between point and intVal */
            buf = new StringBuilder(3-insertionPoint + intString.length());
            buf.append(signum<0 ? "-0." : "0.");
            for (int i=0; i<-insertionPoint; i++)
                buf.append('0');
            buf.append(intString);
        }
        return buf.toString();
    }

    /**
     * Converts this {@code BigDecimal} to a {@code BigInteger}.
     * This conversion is analogous to the
     * <i>narrowing primitive conversion</i> from {@code double} to
     * {@code long} as defined in section 5.1.3 of
     * <cite>The Java&trade; Language Specification</cite>:
     * any fractional part of this
     * {@code BigDecimal} will be discarded.  Note that this
     * conversion can lose information about the precision of the
     * {@code BigDecimal} value.
     * <p>
     * To have an exception thrown if the conversion is inexact (in
     * other words if a nonzero fractional part is discarded), use the
     * {@link #toBigIntegerExact()} method.
     *
     * @return this {@code BigDecimal} converted to a {@code BigInteger}.
     */
    public BigInteger toBigInteger() {
        // force to an integer, quietly
        return this.setScale(0, ROUND_DOWN).inflated();
    }

    /**
     * Converts this {@code BigDecimal} to a {@code BigInteger},
     * checking for lost information.  An exception is thrown if this
     * {@code BigDecimal} has a nonzero fractional part.
     *
     * @return this {@code BigDecimal} converted to a {@code BigInteger}.
     * @throws ArithmeticException if {@code this} has a nonzero
     *         fractional part.
     * @since  1.5
     */
    public BigInteger toBigIntegerExact() {
        // round to an integer, with Exception if decimal part non-0
        return this.setScale(0, ROUND_UNNECESSARY).inflated();
    }

    /**
     * Converts this {@code BigDecimal} to a {@code long}.
     * This conversion is analogous to the
     * <i>narrowing primitive conversion</i> from {@code double} to
     * {@code short} as defined in section 5.1.3 of
     * <cite>The Java&trade; Language Specification</cite>:
     * any fractional part of this
     * {@code BigDecimal} will be discarded, and if the resulting
     * "{@code BigInteger}" is too big to fit in a
     * {@code long}, only the low-order 64 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude and precision of this {@code BigDecimal} value as well
     * as return a result with the opposite sign.
     *
     * @return this {@code BigDecimal} converted to a {@code long}.
     */
    public long longValue(){
        if (intCompact != INFLATED && scale == 0) {
            return intCompact;
        } else {
            // Fastpath zero and small values
            if (this.signum() == 0 || fractionOnly() ||
                // Fastpath very large-scale values that will result
                // in a truncated value of zero. If the scale is -64
                // or less, there are at least 64 powers of 10 in the
                // value of the numerical result. Since 10 = 2*5, in
                // that case there would also be 64 powers of 2 in the
                // result, meaning all 64 bits of a long will be zero.
                scale <= -64) {
                return 0;
            } else {
                return toBigInteger().longValue();
            }
        }
    }

    /**
     * Return true if a nonzero BigDecimal has an absolute value less
     * than one; i.e. only has fraction digits.
     */
    private boolean fractionOnly() {
        assert this.signum() != 0;
        return (this.precision() - this.scale) <= 0;
    }

    /**
     * Converts this {@code BigDecimal} to a {@code long}, checking
     * for lost information.  If this {@code BigDecimal} has a
     * nonzero fractional part or is out of the possible range for a
     * {@code long} result then an {@code ArithmeticException} is
     * thrown.
     *
     * @return this {@code BigDecimal} converted to a {@code long}.
     * @throws ArithmeticException if {@code this} has a nonzero
     *         fractional part, or will not fit in a {@code long}.
     * @since  1.5
     */
    public long longValueExact() {
        if (intCompact != INFLATED && scale == 0)
            return intCompact;

        // Fastpath zero
        if (this.signum() == 0)
            return 0;

        // Fastpath numbers less than 1.0 (the latter can be very slow
        // to round if very small)
        if (fractionOnly())
            throw new ArithmeticException("Rounding necessary");

        // If more than 19 digits in integer part it cannot possibly fit
        if ((precision() - scale) > 19) // [OK for negative scale too]
            throw new java.lang.ArithmeticException("Overflow");

        // round to an integer, with Exception if decimal part non-0
        BigDecimal num = this.setScale(0, ROUND_UNNECESSARY);
        if (num.precision() >= 19) // need to check carefully
            LongOverflow.check(num);
        return num.inflated().longValue();
    }

    private static class LongOverflow {
        /** BigInteger equal to Long.MIN_VALUE. */
        private static final BigInteger LONGMIN = BigInteger.valueOf(Long.MIN_VALUE);

        /** BigInteger equal to Long.MAX_VALUE. */
        private static final BigInteger LONGMAX = BigInteger.valueOf(Long.MAX_VALUE);

        public static void check(BigDecimal num) {
            BigInteger intVal = num.inflated();
            if (intVal.compareTo(LONGMIN) < 0 ||
                intVal.compareTo(LONGMAX) > 0)
                throw new java.lang.ArithmeticException("Overflow");
        }
    }

    /**
     * Converts this {@code BigDecimal} to an {@code int}.
     * This conversion is analogous to the
     * <i>narrowing primitive conversion</i> from {@code double} to
     * {@code short} as defined in section 5.1.3 of
     * <cite>The Java&trade; Language Specification</cite>:
     * any fractional part of this
     * {@code BigDecimal} will be discarded, and if the resulting
     * "{@code BigInteger}" is too big to fit in an
     * {@code int}, only the low-order 32 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude and precision of this {@code BigDecimal}
     * value as well as return a result with the opposite sign.
     *
     * @return this {@code BigDecimal} converted to an {@code int}.
     */
    public int intValue() {
        return  (intCompact != INFLATED && scale == 0) ?
            (int)intCompact :
            (int)longValue();
    }

    /**
     * Converts this {@code BigDecimal} to an {@code int}, checking
     * for lost information.  If this {@code BigDecimal} has a
     * nonzero fractional part or is out of the possible range for an
     * {@code int} result then an {@code ArithmeticException} is
     * thrown.
     *
     * @return this {@code BigDecimal} converted to an {@code int}.
     * @throws ArithmeticException if {@code this} has a nonzero
     *         fractional part, or will not fit in an {@code int}.
     * @since  1.5
     */
    public int intValueExact() {
       long num;
       num = this.longValueExact();     // will check decimal part
       if ((int)num != num)
           throw new java.lang.ArithmeticException("Overflow");
       return (int)num;
    }

    /**
     * Converts this {@code BigDecimal} to a {@code short}, checking
     * for lost information.  If this {@code BigDecimal} has a
     * nonzero fractional part or is out of the possible range for a
     * {@code short} result then an {@code ArithmeticException} is
     * thrown.
     *
     * @return this {@code BigDecimal} converted to a {@code short}.
     * @throws ArithmeticException if {@code this} has a nonzero
     *         fractional part, or will not fit in a {@code short}.
     * @since  1.5
     */
    public short shortValueExact() {
       long num;
       num = this.longValueExact();     // will check decimal part
       if ((short)num != num)
           throw new java.lang.ArithmeticException("Overflow");
       return (short)num;
    }

    /**
     * Converts this {@code BigDecimal} to a {@code byte}, checking
     * for lost information.  If this {@code BigDecimal} has a
     * nonzero fractional part or is out of the possible range for a
     * {@code byte} result then an {@code ArithmeticException} is
     * thrown.
     *
     * @return this {@code BigDecimal} converted to a {@code byte}.
     * @throws ArithmeticException if {@code this} has a nonzero
     *         fractional part, or will not fit in a {@code byte}.
     * @since  1.5
     */
    public byte byteValueExact() {
       long num;
       num = this.longValueExact();     // will check decimal part
       if ((byte)num != num)
           throw new java.lang.ArithmeticException("Overflow");
       return (byte)num;
    }

    /**
     * Converts this {@code BigDecimal} to a {@code float}.
     * This conversion is similar to the
     * <i>narrowing primitive conversion</i> from {@code double} to
     * {@code float} as defined in section 5.1.3 of
     * <cite>The Java&trade; Language Specification</cite>:
     * if this {@code BigDecimal} has too great a
     * magnitude to represent as a {@code float}, it will be
     * converted to {@link Float#NEGATIVE_INFINITY} or {@link
     * Float#POSITIVE_INFINITY} as appropriate.  Note that even when
     * the return value is finite, this conversion can lose
     * information about the precision of the {@code BigDecimal}
     * value.
     *
     * @return this {@code BigDecimal} converted to a {@code float}.
     */
    public float floatValue(){
        if(intCompact != INFLATED) {
            if (scale == 0) {
                return (float)intCompact;
            } else {
                /*
                 * If both intCompact and the scale can be exactly
                 * represented as float values, perform a single float
                 * multiply or divide to compute the (properly
                 * rounded) result.
                 */
                if (Math.abs(intCompact) < 1L<<22 ) {
                    // Don't have too guard against
                    // Math.abs(MIN_VALUE) because of outer check
                    // against INFLATED.
                    if (scale > 0 && scale < float10pow.length) {
                        return (float)intCompact / float10pow[scale];
                    } else if (scale < 0 && scale > -float10pow.length) {
                        return (float)intCompact * float10pow[-scale];
                    }
                }
            }
        }
        // Somewhat inefficient, but guaranteed to work.
        return Float.parseFloat(this.toString());
    }

    /**
     * Converts this {@code BigDecimal} to a {@code double}.
     * This conversion is similar to the
     * <i>narrowing primitive conversion</i> from {@code double} to
     * {@code float} as defined in section 5.1.3 of
     * <cite>The Java&trade; Language Specification</cite>:
     * if this {@code BigDecimal} has too great a
     * magnitude represent as a {@code double}, it will be
     * converted to {@link Double#NEGATIVE_INFINITY} or {@link
     * Double#POSITIVE_INFINITY} as appropriate.  Note that even when
     * the return value is finite, this conversion can lose
     * information about the precision of the {@code BigDecimal}
     * value.
     *
     * @return this {@code BigDecimal} converted to a {@code double}.
     */
    public double doubleValue(){
        if(intCompact != INFLATED) {
            if (scale == 0) {
                return (double)intCompact;
            } else {
                /*
                 * If both intCompact and the scale can be exactly
                 * represented as double values, perform a single
                 * double multiply or divide to compute the (properly
                 * rounded) result.
                 */
                if (Math.abs(intCompact) < 1L<<52 ) {
                    // Don't have too guard against
                    // Math.abs(MIN_VALUE) because of outer check
                    // against INFLATED.
                    if (scale > 0 && scale < double10pow.length) {
                        return (double)intCompact / double10pow[scale];
                    } else if (scale < 0 && scale > -double10pow.length) {
                        return (double)intCompact * double10pow[-scale];
                    }
                }
            }
        }
        // Somewhat inefficient, but guaranteed to work.
        return Double.parseDouble(this.toString());
    }

    /**
     * Powers of 10 which can be represented exactly in {@code
     * double}.
     */
    private static final double double10pow[] = {
        1.0e0,  1.0e1,  1.0e2,  1.0e3,  1.0e4,  1.0e5,
        1.0e6,  1.0e7,  1.0e8,  1.0e9,  1.0e10, 1.0e11,
        1.0e12, 1.0e13, 1.0e14, 1.0e15, 1.0e16, 1.0e17,
        1.0e18, 1.0e19, 1.0e20, 1.0e21, 1.0e22
    };

    /**
     * Powers of 10 which can be represented exactly in {@code
     * float}.
     */
    private static final float float10pow[] = {
        1.0e0f, 1.0e1f, 1.0e2f, 1.0e3f, 1.0e4f, 1.0e5f,
        1.0e6f, 1.0e7f, 1.0e8f, 1.0e9f, 1.0e10f
    };

    /**
     * Returns the size of an ulp, a unit in the last place, of this
     * {@code BigDecimal}.  An ulp of a nonzero {@code BigDecimal}
     * value is the positive distance between this value and the
     * {@code BigDecimal} value next larger in magnitude with the
     * same number of digits.  An ulp of a zero value is numerically
     * equal to 1 with the scale of {@code this}.  The result is
     * stored with the same scale as {@code this} so the result
     * for zero and nonzero values is equal to {@code [1,
     * this.scale()]}.
     *
     * @return the size of an ulp of {@code this}
     * @since 1.5
     */
    public BigDecimal ulp() {
        return BigDecimal.valueOf(1, this.scale(), 1);
    }

    // Private class to build a string representation for BigDecimal object.
    // "StringBuilderHelper" is constructed as a thread local variable so it is
    // thread safe. The StringBuilder field acts as a buffer to hold the temporary
    // representation of BigDecimal. The cmpCharArray holds all the characters for
    // the compact representation of BigDecimal (except for '-' sign' if it is
    // negative) if its intCompact field is not INFLATED. It is shared by all
    // calls to toString() and its variants in that particular thread.
    static class StringBuilderHelper {
        final StringBuilder sb;    // Placeholder for BigDecimal string
        final char[] cmpCharArray; // character array to place the intCompact

        StringBuilderHelper() {
            sb = new StringBuilder();
            // All non negative longs can be made to fit into 19 character array.
            cmpCharArray = new char[19];
        }

        // Accessors.
        StringBuilder getStringBuilder() {
            sb.setLength(0);
            return sb;
        }

        char[] getCompactCharArray() {
            return cmpCharArray;
        }

        /**
         * Places characters representing the intCompact in {@code long} into
         * cmpCharArray and returns the offset to the array where the
         * representation starts.
         *
         * @param intCompact the number to put into the cmpCharArray.
         * @return offset to the array where the representation starts.
         * Note: intCompact must be greater or equal to zero.
         */
        int putIntCompact(long intCompact) {
            assert intCompact >= 0;

            long q;
            int r;
            // since we start from the least significant digit, charPos points to
            // the last character in cmpCharArray.
            int charPos = cmpCharArray.length;

            // Get 2 digits/iteration using longs until quotient fits into an int
            while (intCompact > Integer.MAX_VALUE) {
                q = intCompact / 100;
                r = (int)(intCompact - q * 100);
                intCompact = q;
                cmpCharArray[--charPos] = DIGIT_ONES[r];
                cmpCharArray[--charPos] = DIGIT_TENS[r];
            }

            // Get 2 digits/iteration using ints when i2 >= 100
            int q2;
            int i2 = (int)intCompact;
            while (i2 >= 100) {
                q2 = i2 / 100;
                r  = i2 - q2 * 100;
                i2 = q2;
                cmpCharArray[--charPos] = DIGIT_ONES[r];
                cmpCharArray[--charPos] = DIGIT_TENS[r];
            }

            cmpCharArray[--charPos] = DIGIT_ONES[i2];
            if (i2 >= 10)
                cmpCharArray[--charPos] = DIGIT_TENS[i2];

            return charPos;
        }

        final static char[] DIGIT_TENS = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
        };

        final static char[] DIGIT_ONES = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        };
    }

    /**
     * Lay out this {@code BigDecimal} into a {@code char[]} array.
     * The Java 1.2 equivalent to this was called {@code getValueString}.
     *
     * @param  sci {@code true} for Scientific exponential notation;
     *          {@code false} for Engineering
     * @return string with canonical string representation of this
     *         {@code BigDecimal}
     */
    private String layoutChars(boolean sci) {
        if (scale == 0)                      // zero scale is trivial
            return (intCompact != INFLATED) ?
                Long.toString(intCompact):
                intVal.toString();
        if (scale == 2  &&
            intCompact >= 0 && intCompact < Integer.MAX_VALUE) {
            // currency fast path
            int lowInt = (int)intCompact % 100;
            int highInt = (int)intCompact / 100;
            return (Integer.toString(highInt) + '.' +
                    StringBuilderHelper.DIGIT_TENS[lowInt] +
                    StringBuilderHelper.DIGIT_ONES[lowInt]) ;
        }

        StringBuilderHelper sbHelper = threadLocalStringBuilderHelper.get();
        char[] coeff;
        int offset;  // offset is the starting index for coeff array
        // Get the significand as an absolute value
        if (intCompact != INFLATED) {
            offset = sbHelper.putIntCompact(Math.abs(intCompact));
            coeff  = sbHelper.getCompactCharArray();
        } else {
            offset = 0;
            coeff  = intVal.abs().toString().toCharArray();
        }

        // Construct a buffer, with sufficient capacity for all cases.
        // If E-notation is needed, length will be: +1 if negative, +1
        // if '.' needed, +2 for "E+", + up to 10 for adjusted exponent.
        // Otherwise it could have +1 if negative, plus leading "0.00000"
        StringBuilder buf = sbHelper.getStringBuilder();
        if (signum() < 0)             // prefix '-' if negative
            buf.append('-');
        int coeffLen = coeff.length - offset;
        long adjusted = -(long)scale + (coeffLen -1);
        if ((scale >= 0) && (adjusted >= -6)) { // plain number
            int pad = scale - coeffLen;         // count of padding zeros
            if (pad >= 0) {                     // 0.xxx form
                buf.append('0');
                buf.append('.');
                for (; pad>0; pad--) {
                    buf.append('0');
                }
                buf.append(coeff, offset, coeffLen);
            } else {                         // xx.xx form
                buf.append(coeff, offset, -pad);
                buf.append('.');
                buf.append(coeff, -pad + offset, scale);
            }
        } else { // E-notation is needed
            if (sci) {                       // Scientific notation
                buf.append(coeff[offset]);   // first character
                if (coeffLen > 1) {          // more to come
                    buf.append('.');
                    buf.append(coeff, offset + 1, coeffLen - 1);
                }
            } else {                         // Engineering notation
                int sig = (int)(adjusted % 3);
                if (sig < 0)
                    sig += 3;                // [adjusted was negative]
                adjusted -= sig;             // now a multiple of 3
                sig++;
                if (signum() == 0) {
                    switch (sig) {
                    case 1:
                        buf.append('0'); // exponent is a multiple of three
                        break;
                    case 2:
                        buf.append("0.00");
                        adjusted += 3;
                        break;
                    case 3:
                        buf.append("0.0");
                        adjusted += 3;
                        break;
                    default:
                        throw new AssertionError("Unexpected sig value " + sig);
                    }
                } else if (sig >= coeffLen) {   // significand all in integer
                    buf.append(coeff, offset, coeffLen);
                    // may need some zeros, too
                    for (int i = sig - coeffLen; i > 0; i--)
                        buf.append('0');
                } else {                     // xx.xxE form
                    buf.append(coeff, offset, sig);
                    buf.append('.');
                    buf.append(coeff, offset + sig, coeffLen - sig);
                }
            }
            if (adjusted != 0) {             // [!sci could have made 0]
                buf.append('E');
                if (adjusted > 0)            // force sign for positive
                    buf.append('+');
                buf.append(adjusted);
            }
        }
        return buf.toString();
    }

    /**
     * Return 10 to the power n, as a {@code BigInteger}.
     *
     * @param  n the power of ten to be returned (>=0)
     * @return a {@code BigInteger} with the value (10<sup>n</sup>)
     */
    private static BigInteger bigTenToThe(int n) {
        if (n < 0)
            return BigInteger.ZERO;

        if (n < BIG_TEN_POWERS_TABLE_MAX) {
            BigInteger[] pows = BIG_TEN_POWERS_TABLE;
            if (n < pows.length)
                return pows[n];
            else
                return expandBigIntegerTenPowers(n);
        }

        return BigInteger.TEN.pow(n);
    }

    /**
     * Expand the BIG_TEN_POWERS_TABLE array to contain at least 10**n.
     *
     * @param n the power of ten to be returned (>=0)
     * @return a {@code BigDecimal} with the value (10<sup>n</sup>) and
     *         in the meantime, the BIG_TEN_POWERS_TABLE array gets
     *         expanded to the size greater than n.
     */
    private static BigInteger expandBigIntegerTenPowers(int n) {
        synchronized(BigDecimal.class) {
            BigInteger[] pows = BIG_TEN_POWERS_TABLE;
            int curLen = pows.length;
            // The following comparison and the above synchronized statement is
            // to prevent multiple threads from expanding the same array.
            if (curLen <= n) {
                int newLen = curLen << 1;
                while (newLen <= n)
                    newLen <<= 1;
                pows = Arrays.copyOf(pows, newLen);
                for (int i = curLen; i < newLen; i++)
                    pows[i] = pows[i - 1].multiply(BigInteger.TEN);
                // Based on the following facts:
                // 1. pows is a private local varible;
                // 2. the following store is a volatile store.
                // the newly created array elements can be safely published.
                BIG_TEN_POWERS_TABLE = pows;
            }
            return pows[n];
        }
    }

    private static final long[] LONG_TEN_POWERS_TABLE = {
        1,                     // 0 / 10^0
        10,                    // 1 / 10^1
        100,                   // 2 / 10^2
        1000,                  // 3 / 10^3
        10000,                 // 4 / 10^4
        100000,                // 5 / 10^5
        1000000,               // 6 / 10^6
        10000000,              // 7 / 10^7
        100000000,             // 8 / 10^8
        1000000000,            // 9 / 10^9
        10000000000L,          // 10 / 10^10
        100000000000L,         // 11 / 10^11
        1000000000000L,        // 12 / 10^12
        10000000000000L,       // 13 / 10^13
        100000000000000L,      // 14 / 10^14
        1000000000000000L,     // 15 / 10^15
        10000000000000000L,    // 16 / 10^16
        100000000000000000L,   // 17 / 10^17
        1000000000000000000L   // 18 / 10^18
    };

    private static volatile BigInteger BIG_TEN_POWERS_TABLE[] = {
        BigInteger.ONE,
        BigInteger.valueOf(10),
        BigInteger.valueOf(100),
        BigInteger.valueOf(1000),
        BigInteger.valueOf(10000),
        BigInteger.valueOf(100000),
        BigInteger.valueOf(1000000),
        BigInteger.valueOf(10000000),
        BigInteger.valueOf(100000000),
        BigInteger.valueOf(1000000000),
        BigInteger.valueOf(10000000000L),
        BigInteger.valueOf(100000000000L),
        BigInteger.valueOf(1000000000000L),
        BigInteger.valueOf(10000000000000L),
        BigInteger.valueOf(100000000000000L),
        BigInteger.valueOf(1000000000000000L),
        BigInteger.valueOf(10000000000000000L),
        BigInteger.valueOf(100000000000000000L),
        BigInteger.valueOf(1000000000000000000L)
    };

    private static final int BIG_TEN_POWERS_TABLE_INITLEN =
        BIG_TEN_POWERS_TABLE.length;
    private static final int BIG_TEN_POWERS_TABLE_MAX =
        16 * BIG_TEN_POWERS_TABLE_INITLEN;

    private static final long THRESHOLDS_TABLE[] = {
        Long.MAX_VALUE,                     // 0
        Long.MAX_VALUE/10L,                 // 1
        Long.MAX_VALUE/100L,                // 2
        Long.MAX_VALUE/1000L,               // 3
        Long.MAX_VALUE/10000L,              // 4
        Long.MAX_VALUE/100000L,             // 5
        Long.MAX_VALUE/1000000L,            // 6
        Long.MAX_VALUE/10000000L,           // 7
        Long.MAX_VALUE/100000000L,          // 8
        Long.MAX_VALUE/1000000000L,         // 9
        Long.MAX_VALUE/10000000000L,        // 10
        Long.MAX_VALUE/100000000000L,       // 11
        Long.MAX_VALUE/1000000000000L,      // 12
        Long.MAX_VALUE/10000000000000L,     // 13
        Long.MAX_VALUE/100000000000000L,    // 14
        Long.MAX_VALUE/1000000000000000L,   // 15
        Long.MAX_VALUE/10000000000000000L,  // 16
        Long.MAX_VALUE/100000000000000000L, // 17
        Long.MAX_VALUE/1000000000000000000L // 18
    };

    /**
     * Compute val * 10 ^ n; return this product if it is
     * representable as a long, INFLATED otherwise.
     */
    private static long longMultiplyPowerTen(long val, int n) {
        if (val == 0 || n <= 0)
            return val;
        long[] tab = LONG_TEN_POWERS_TABLE;
        long[] bounds = THRESHOLDS_TABLE;
        if (n < tab.length && n < bounds.length) {
            long tenpower = tab[n];
            if (val == 1)
                return tenpower;
            if (Math.abs(val) <= bounds[n])
                return val * tenpower;
        }
        return INFLATED;
    }

    /**
     * Compute this * 10 ^ n.
     * Needed mainly to allow special casing to trap zero value
     */
    private BigInteger bigMultiplyPowerTen(int n) {
        if (n <= 0)
            return this.inflated();

        if (intCompact != INFLATED)
            return bigTenToThe(n).multiply(intCompact);
        else
            return intVal.multiply(bigTenToThe(n));
    }

    /**
     * Returns appropriate BigInteger from intVal field if intVal is
     * null, i.e. the compact representation is in use.
     */
    private BigInteger inflated() {
        if (intVal == null) {
            return BigInteger.valueOf(intCompact);
        }
        return intVal;
    }

    /**
     * Match the scales of two {@code BigDecimal}s to align their
     * least significant digits.
     *
     * <p>If the scales of val[0] and val[1] differ, rescale
     * (non-destructively) the lower-scaled {@code BigDecimal} so
     * they match.  That is, the lower-scaled reference will be
     * replaced by a reference to a new object with the same scale as
     * the other {@code BigDecimal}.
     *
     * @param  val array of two elements referring to the two
     *         {@code BigDecimal}s to be aligned.
     */
    private static void matchScale(BigDecimal[] val) {
        if (val[0].scale == val[1].scale) {
            return;
        } else if (val[0].scale < val[1].scale) {
            val[0] = val[0].setScale(val[1].scale, ROUND_UNNECESSARY);
        } else if (val[1].scale < val[0].scale) {
            val[1] = val[1].setScale(val[0].scale, ROUND_UNNECESSARY);
        }
    }

    private static class UnsafeHolder {
        private static final sun.misc.Unsafe unsafe;
        private static final long intCompactOffset;
        private static final long intValOffset;
        private static final long scaleOffset;
        static {
            try {
                unsafe = sun.misc.Unsafe.getUnsafe();
                intCompactOffset = unsafe.objectFieldOffset
                    (BigDecimal.class.getDeclaredField("intCompact"));
                intValOffset = unsafe.objectFieldOffset
                    (BigDecimal.class.getDeclaredField("intVal"));
                scaleOffset = unsafe.objectFieldOffset
                    (BigDecimal.class.getDeclaredField("scale"));
            } catch (Exception ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }
        static void setIntValAndScale(BigDecimal bd, BigInteger intVal, int scale) {
            unsafe.putObject(bd, intValOffset, intVal);
            unsafe.putInt(bd, scaleOffset, scale);
            unsafe.putLong(bd, intCompactOffset, compactValFor(intVal));
        }

        static void setIntValVolatile(BigDecimal bd, BigInteger val) {
            unsafe.putObjectVolatile(bd, intValOffset, val);
        }
    }

    /**
     * Reconstitute the {@code BigDecimal} instance from a stream (that is,
     * deserialize it).
     *
     * @param s the stream being read.
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // prepare to read the fields
        ObjectInputStream.GetField fields = s.readFields();
        BigInteger serialIntVal = (BigInteger) fields.get("intVal", null);

        // Validate field data
        if (serialIntVal == null) {
            throw new StreamCorruptedException("Null or missing intVal in BigDecimal stream");
        }
        // Validate provenance of serialIntVal object
        serialIntVal = toStrictBigInteger(serialIntVal);

        // Any integer value is valid for scale
        int serialScale = fields.get("scale", 0);

        UnsafeHolder.setIntValAndScale(this, serialIntVal, serialScale);
    }

    /**
     * Serialization without data not supported for this class.
     */
    private void readObjectNoData()
        throws ObjectStreamException {
        throw new InvalidObjectException("Deserialized BigDecimal objects need data");
    }

   /**
    * Serialize this {@code BigDecimal} to the stream in question
    *
    * @param s the stream to serialize to.
    */
   private void writeObject(java.io.ObjectOutputStream s)
       throws java.io.IOException {
       // Must inflate to maintain compatible serial form.
       if (this.intVal == null)
           UnsafeHolder.setIntValVolatile(this, BigInteger.valueOf(this.intCompact));
       // Could reset intVal back to null if it has to be set.
       s.defaultWriteObject();
   }

    /**
     * Returns the length of the absolute value of a {@code long}, in decimal
     * digits.
     *
     * @param x the {@code long}
     * @return the length of the unscaled value, in deciaml digits.
     */
    static int longDigitLength(long x) {
        /*
         * As described in "Bit Twiddling Hacks" by Sean Anderson,
         * (http://graphics.stanford.edu/~seander/bithacks.html)
         * integer log 10 of x is within 1 of (1233/4096)* (1 +
         * integer log 2 of x). The fraction 1233/4096 approximates
         * log10(2). So we first do a version of log2 (a variant of
         * Long class with pre-checks and opposite directionality) and
         * then scale and check against powers table. This is a little
         * simpler in present context than the version in Hacker's
         * Delight sec 11-4. Adding one to bit length allows comparing
         * downward from the LONG_TEN_POWERS_TABLE that we need
         * anyway.
         */
        assert x != BigDecimal.INFLATED;
        if (x < 0)
            x = -x;
        if (x < 10) // must screen for 0, might as well 10
            return 1;
        int r = ((64 - Long.numberOfLeadingZeros(x) + 1) * 1233) >>> 12;
        long[] tab = LONG_TEN_POWERS_TABLE;
        // if r >= length, must have max possible digits for long
        return (r >= tab.length || x < tab[r]) ? r : r + 1;
    }

    /**
     * Returns the length of the absolute value of a BigInteger, in
     * decimal digits.
     *
     * @param b the BigInteger
     * @return the length of the unscaled value, in decimal digits
     */
    private static int bigDigitLength(BigInteger b) {
        /*
         * Same idea as the long version, but we need a better
         * approximation of log10(2). Using 646456993/2^31
         * is accurate up to max possible reported bitLength.
         */
        if (b.signum == 0)
            return 1;
        int r = (int)((((long)b.bitLength() + 1) * 646456993) >>> 31);
        return b.compareMagnitude(bigTenToThe(r)) < 0? r : r+1;
    }

    /**
     * Check a scale for Underflow or Overflow.  If this BigDecimal is
     * nonzero, throw an exception if the scale is outof range. If this
     * is zero, saturate the scale to the extreme value of the right
     * sign if the scale is out of range.
     *
     * @param val The new scale.
     * @throws ArithmeticException (overflow or underflow) if the new
     *         scale is out of range.
     * @return validated scale as an int.
     */
    private int checkScale(long val) {
        int asInt = (int)val;
        if (asInt != val) {
            asInt = val>Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            BigInteger b;
            if (intCompact != 0 &&
                ((b = intVal) == null || b.signum() != 0))
                throw new ArithmeticException(asInt>0 ? "Underflow":"Overflow");
        }
        return asInt;
    }

   /**
     * Returns the compact value for given {@code BigInteger}, or
     * INFLATED if too big. Relies on internal representation of
     * {@code BigInteger}.
     */
    private static long compactValFor(BigInteger b) {
        int[] m = b.mag;
        int len = m.length;
        if (len == 0)
            return 0;
        int d = m[0];
        if (len > 2 || (len == 2 && d < 0))
            return INFLATED;

        long u = (len == 2)?
            (((long) m[1] & LONG_MASK) + (((long)d) << 32)) :
            (((long)d)   & LONG_MASK);
        return (b.signum < 0)? -u : u;
    }

    private static int longCompareMagnitude(long x, long y) {
        if (x < 0)
            x = -x;
        if (y < 0)
            y = -y;
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    private static int saturateLong(long s) {
        int i = (int)s;
        return (s == i) ? i : (s < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE);
    }

    /*
     * Internal printing routine
     */
    private static void print(String name, BigDecimal bd) {
        System.err.format("%s:\tintCompact %d\tintVal %d\tscale %d\tprecision %d%n",
                          name,
                          bd.intCompact,
                          bd.intVal,
                          bd.scale,
                          bd.precision);
    }

    /**
     * Check internal invariants of this BigDecimal.  These invariants
     * include:
     *
     * <ul>
     *
     * <li>The object must be initialized; either intCompact must not be
     * INFLATED or intVal is non-null.  Both of these conditions may
     * be true.
     *
     * <li>If both intCompact and intVal and set, their values must be
     * consistent.
     *
     * <li>If precision is nonzero, it must have the right value.
     * </ul>
     *
     * Note: Since this is an audit method, we are not supposed to change the
     * state of this BigDecimal object.
     */
    private BigDecimal audit() {
        if (intCompact == INFLATED) {
            if (intVal == null) {
                print("audit", this);
                throw new AssertionError("null intVal");
            }
            // Check precision
            if (precision > 0 && precision != bigDigitLength(intVal)) {
                print("audit", this);
                throw new AssertionError("precision mismatch");
            }
        } else {
            if (intVal != null) {
                long val = intVal.longValue();
                if (val != intCompact) {
                    print("audit", this);
                    throw new AssertionError("Inconsistent state, intCompact=" +
                                             intCompact + "\t intVal=" + val);
                }
            }
            // Check precision
            if (precision > 0 && precision != longDigitLength(intCompact)) {
                print("audit", this);
                throw new AssertionError("precision mismatch");
            }
        }
        return this;
    }

    /* the same as checkScale where value!=0 */
    private static int checkScaleNonZero(long val) {
        int asInt = (int)val;
        if (asInt != val) {
            throw new ArithmeticException(asInt>0 ? "Underflow":"Overflow");
        }
        return asInt;
    }

    private static int checkScale(long intCompact, long val) {
        int asInt = (int)val;
        if (asInt != val) {
            asInt = val>Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            if (intCompact != 0)
                throw new ArithmeticException(asInt>0 ? "Underflow":"Overflow");
        }
        return asInt;
    }

    private static int checkScale(BigInteger intVal, long val) {
        int asInt = (int)val;
        if (asInt != val) {
            asInt = val>Integer.MAX_VALUE ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            if (intVal.signum() != 0)
                throw new ArithmeticException(asInt>0 ? "Underflow":"Overflow");
        }
        return asInt;
    }

    /**
     * Returns a {@code BigDecimal} rounded according to the MathContext
     * settings;
     * If rounding is needed a new {@code BigDecimal} is created and returned.
     *
     * @param val the value to be rounded
     * @param mc the context to use.
     * @return a {@code BigDecimal} rounded according to the MathContext
     *         settings.  May return {@code value}, if no rounding needed.
     * @throws ArithmeticException if the rounding mode is
     *         {@code RoundingMode.UNNECESSARY} and the
     *         result is inexact.
     */
    private static BigDecimal doRound(BigDecimal val, MathContext mc) {
        int mcp = mc.precision;
        boolean wasDivided = false;
        if (mcp > 0) {
            BigInteger intVal = val.intVal;
            long compactVal = val.intCompact;
            int scale = val.scale;
            int prec = val.precision();
            int mode = mc.roundingMode.oldMode;
            int drop;
            if (compactVal == INFLATED) {
                drop = prec - mcp;
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    intVal = divideAndRoundByTenPow(intVal, drop, mode);
                    wasDivided = true;
                    compactVal = compactValFor(intVal);
                    if (compactVal != INFLATED) {
                        prec = longDigitLength(compactVal);
                        break;
                    }
                    prec = bigDigitLength(intVal);
                    drop = prec - mcp;
                }
            }
            if (compactVal != INFLATED) {
                drop = prec - mcp;  // drop can't be more than 18
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                    wasDivided = true;
                    prec = longDigitLength(compactVal);
                    drop = prec - mcp;
                    intVal = null;
                }
            }
            return wasDivided ? new BigDecimal(intVal,compactVal,scale,prec) : val;
        }
        return val;
    }

    /*
     * Returns a {@code BigDecimal} created from {@code long} value with
     * given scale rounded according to the MathContext settings
     */
    private static BigDecimal doRound(long compactVal, int scale, MathContext mc) {
        int mcp = mc.precision;
        if (mcp > 0 && mcp < 19) {
            int prec = longDigitLength(compactVal);
            int drop = prec - mcp;  // drop can't be more than 18
            while (drop > 0) {
                scale = checkScaleNonZero((long) scale - drop);
                compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                prec = longDigitLength(compactVal);
                drop = prec - mcp;
            }
            return valueOf(compactVal, scale, prec);
        }
        return valueOf(compactVal, scale);
    }

    /*
     * Returns a {@code BigDecimal} created from {@code BigInteger} value with
     * given scale rounded according to the MathContext settings
     */
    private static BigDecimal doRound(BigInteger intVal, int scale, MathContext mc) {
        int mcp = mc.precision;
        int prec = 0;
        if (mcp > 0) {
            long compactVal = compactValFor(intVal);
            int mode = mc.roundingMode.oldMode;
            int drop;
            if (compactVal == INFLATED) {
                prec = bigDigitLength(intVal);
                drop = prec - mcp;
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    intVal = divideAndRoundByTenPow(intVal, drop, mode);
                    compactVal = compactValFor(intVal);
                    if (compactVal != INFLATED) {
                        break;
                    }
                    prec = bigDigitLength(intVal);
                    drop = prec - mcp;
                }
            }
            if (compactVal != INFLATED) {
                prec = longDigitLength(compactVal);
                drop = prec - mcp;     // drop can't be more than 18
                while (drop > 0) {
                    scale = checkScaleNonZero((long) scale - drop);
                    compactVal = divideAndRound(compactVal, LONG_TEN_POWERS_TABLE[drop], mc.roundingMode.oldMode);
                    prec = longDigitLength(compactVal);
                    drop = prec - mcp;
                }
                return valueOf(compactVal,scale,prec);
            }
        }
        return new BigDecimal(intVal,INFLATED,scale,prec);
    }

    /*
     * Divides {@code BigInteger} value by ten power.
     */
    private static BigInteger divideAndRoundByTenPow(BigInteger intVal, int tenPow, int roundingMode) {
        if (tenPow < LONG_TEN_POWERS_TABLE.length)
            intVal = divideAndRound(intVal, LONG_TEN_POWERS_TABLE[tenPow], roundingMode);
        else
            intVal = divideAndRound(intVal, bigTenToThe(tenPow), roundingMode);
        return intVal;
    }

    /**
     * Internally used for division operation for division {@code long} by
     * {@code long}.
     * The returned {@code BigDecimal} object is the quotient whose scale is set
     * to the passed in scale. If the remainder is not zero, it will be rounded
     * based on the passed in roundingMode. Also, if the remainder is zero and
     * the last parameter, i.e. preferredScale is NOT equal to scale, the
     * trailing zeros of the result is stripped to match the preferredScale.
     */
    private static BigDecimal divideAndRound(long ldividend, long ldivisor, int scale, int roundingMode,
                                             int preferredScale) {

        int qsign; // quotient sign
        long q = ldividend / ldivisor; // store quotient in long
        if (roundingMode == ROUND_DOWN && scale == preferredScale)
            return valueOf(q, scale);
        long r = ldividend % ldivisor; // store remainder in long
        qsign = ((ldividend < 0) == (ldivisor < 0)) ? 1 : -1;
        if (r != 0) {
            boolean increment = needIncrement(ldivisor, roundingMode, qsign, q, r);
            return valueOf((increment ? q + qsign : q), scale);
        } else {
            if (preferredScale != scale)
                return createAndStripZerosToMatchScale(q, scale, preferredScale);
            else
                return valueOf(q, scale);
        }
    }

    /**
     * Divides {@code long} by {@code long} and do rounding based on the
     * passed in roundingMode.
     */
    private static long divideAndRound(long ldividend, long ldivisor, int roundingMode) {
        int qsign; // quotient sign
        long q = ldividend / ldivisor; // store quotient in long
        if (roundingMode == ROUND_DOWN)
            return q;
        long r = ldividend % ldivisor; // store remainder in long
        qsign = ((ldividend < 0) == (ldivisor < 0)) ? 1 : -1;
        if (r != 0) {
            boolean increment = needIncrement(ldivisor, roundingMode, qsign, q,     r);
            return increment ? q + qsign : q;
        } else {
            return q;
        }
    }

    /**
     * Shared logic of need increment computation.
     */
    private static boolean commonNeedIncrement(int roundingMode, int qsign,
                                        int cmpFracHalf, boolean oddQuot) {
        switch(roundingMode) {
        case ROUND_UNNECESSARY:
            throw new ArithmeticException("Rounding necessary");

        case ROUND_UP: // Away from zero
            return true;

        case ROUND_DOWN: // Towards zero
            return false;

        case ROUND_CEILING: // Towards +infinity
            return qsign > 0;

        case ROUND_FLOOR: // Towards -infinity
            return qsign < 0;

        default: // Some kind of half-way rounding
            assert roundingMode >= ROUND_HALF_UP &&
                roundingMode <= ROUND_HALF_EVEN: "Unexpected rounding mode" + RoundingMode.valueOf(roundingMode);

            if (cmpFracHalf < 0 ) // We're closer to higher digit
                return false;
            else if (cmpFracHalf > 0 ) // We're closer to lower digit
                return true;
            else { // half-way
                assert cmpFracHalf == 0;

                switch(roundingMode) {
                case ROUND_HALF_DOWN:
                    return false;

                case ROUND_HALF_UP:
                    return true;

                case ROUND_HALF_EVEN:
                    return oddQuot;

                default:
                    throw new AssertionError("Unexpected rounding mode" + roundingMode);
                }
            }
        }
    }

    /**
     * Tests if quotient has to be incremented according the roundingMode
     */
    private static boolean needIncrement(long ldivisor, int roundingMode,
                                         int qsign, long q, long r) {
        assert r != 0L;

        int cmpFracHalf;
        if (r <= HALF_LONG_MIN_VALUE || r > HALF_LONG_MAX_VALUE) {
            cmpFracHalf = 1; // 2 * r can't fit into long
        } else {
            cmpFracHalf = longCompareMagnitude(2 * r, ldivisor);
        }

        return commonNeedIncrement(roundingMode, qsign, cmpFracHalf, (q & 1L) != 0L);
    }

    /**
     * Divides {@code BigInteger} value by {@code long} value and
     * do rounding based on the passed in roundingMode.
     */
    private static BigInteger divideAndRound(BigInteger bdividend, long ldivisor, int roundingMode) {
        boolean isRemainderZero; // record remainder is zero or not
        int qsign; // quotient sign
        long r = 0; // store quotient & remainder in long
        MutableBigInteger mq = null; // store quotient
        // Descend into mutables for faster remainder checks
        MutableBigInteger mdividend = new MutableBigInteger(bdividend.mag);
        mq = new MutableBigInteger();
        r = mdividend.divide(ldivisor, mq);
        isRemainderZero = (r == 0);
        qsign = (ldivisor < 0) ? -bdividend.signum : bdividend.signum;
        if (!isRemainderZero) {
            if(needIncrement(ldivisor, roundingMode, qsign, mq, r)) {
                mq.add(MutableBigInteger.ONE);
            }
        }
        return mq.toBigInteger(qsign);
    }

    /**
     * Internally used for division operation for division {@code BigInteger}
     * by {@code long}.
     * The returned {@code BigDecimal} object is the quotient whose scale is set
     * to the passed in scale. If the remainder is not zero, it will be rounded
     * based on the passed in roundingMode. Also, if the remainder is zero and
     * the last parameter, i.e. preferredScale is NOT equal to scale, the
     * trailing zeros of the result is stripped to match the preferredScale.
     */
    private static BigDecimal divideAndRound(BigInteger bdividend,
                                             long ldivisor, int scale, int roundingMode, int preferredScale) {
        boolean isRemainderZero; // record remainder is zero or not
        int qsign; // quotient sign
        long r = 0; // store quotient & remainder in long
        MutableBigInteger mq = null; // store quotient
        // Descend into mutables for faster remainder checks
        MutableBigInteger mdividend = new MutableBigInteger(bdividend.mag);
        mq = new MutableBigInteger();
        r = mdividend.divide(ldivisor, mq);
        isRemainderZero = (r == 0);
        qsign = (ldivisor < 0) ? -bdividend.signum : bdividend.signum;
        if (!isRemainderZero) {
            if(needIncrement(ldivisor, roundingMode, qsign, mq, r)) {
                mq.add(MutableBigInteger.ONE);
            }
            return mq.toBigDecimal(qsign, scale);
        } else {
            if (preferredScale != scale) {
                long compactVal = mq.toCompactValue(qsign);
                if(compactVal!=INFLATED) {
                    return createAndStripZerosToMatchScale(compactVal, scale, preferredScale);
                }
                BigInteger intVal =  mq.toBigInteger(qsign);
                return createAndStripZerosToMatchScale(intVal,scale, preferredScale);
            } else {
                return mq.toBigDecimal(qsign, scale);
            }
        }
    }

    /**
     * Tests if quotient has to be incremented according the roundingMode
     */
    private static boolean needIncrement(long ldivisor, int roundingMode,
                                         int qsign, MutableBigInteger mq, long r) {
        assert r != 0L;

        int cmpFracHalf;
        if (r <= HALF_LONG_MIN_VALUE || r > HALF_LONG_MAX_VALUE) {
            cmpFracHalf = 1; // 2 * r can't fit into long
        } else {
            cmpFracHalf = longCompareMagnitude(2 * r, ldivisor);
        }

        return commonNeedIncrement(roundingMode, qsign, cmpFracHalf, mq.isOdd());
    }

    /**
     * Divides {@code BigInteger} value by {@code BigInteger} value and
     * do rounding based on the passed in roundingMode.
     */
    private static BigInteger divideAndRound(BigInteger bdividend, BigInteger bdivisor, int roundingMode) {
        boolean isRemainderZero; // record remainder is zero or not
        int qsign; // quotient sign
        // Descend into mutables for faster remainder checks
        MutableBigInteger mdividend = new MutableBigInteger(bdividend.mag);
        MutableBigInteger mq = new MutableBigInteger();
        MutableBigInteger mdivisor = new MutableBigInteger(bdivisor.mag);
        MutableBigInteger mr = mdividend.divide(mdivisor, mq);
        isRemainderZero = mr.isZero();
        qsign = (bdividend.signum != bdivisor.signum) ? -1 : 1;
        if (!isRemainderZero) {
            if (needIncrement(mdivisor, roundingMode, qsign, mq, mr)) {
                mq.add(MutableBigInteger.ONE);
            }
        }
        return mq.toBigInteger(qsign);
    }

    /**
     * Internally used for division operation for division {@code BigInteger}
     * by {@code BigInteger}.
     * The returned {@code BigDecimal} object is the quotient whose scale is set
     * to the passed in scale. If the remainder is not zero, it will be rounded
     * based on the passed in roundingMode. Also, if the remainder is zero and
     * the last parameter, i.e. preferredScale is NOT equal to scale, the
     * trailing zeros of the result is stripped to match the preferredScale.
     */
    private static BigDecimal divideAndRound(BigInteger bdividend, BigInteger bdivisor, int scale, int roundingMode,
                                             int preferredScale) {
        boolean isRemainderZero; // record remainder is zero or not
        int qsign; // quotient sign
        // Descend into mutables for faster remainder checks
        MutableBigInteger mdividend = new MutableBigInteger(bdividend.mag);
        MutableBigInteger mq = new MutableBigInteger();
        MutableBigInteger mdivisor = new MutableBigInteger(bdivisor.mag);
        MutableBigInteger mr = mdividend.divide(mdivisor, mq);
        isRemainderZero = mr.isZero();
        qsign = (bdividend.signum != bdivisor.signum) ? -1 : 1;
        if (!isRemainderZero) {
            if (needIncrement(mdivisor, roundingMode, qsign, mq, mr)) {
                mq.add(MutableBigInteger.ONE);
            }
            return mq.toBigDecimal(qsign, scale);
        } else {
            if (preferredScale != scale) {
                long compactVal = mq.toCompactValue(qsign);
                if (compactVal != INFLATED) {
                    return createAndStripZerosToMatchScale(compactVal, scale, preferredScale);
                }
                BigInteger intVal = mq.toBigInteger(qsign);
                return createAndStripZerosToMatchScale(intVal, scale, preferredScale);
            } else {
                return mq.toBigDecimal(qsign, scale);
            }
        }
    }

    /**
     * Tests if quotient has to be incremented according the roundingMode
     */
    private static boolean needIncrement(MutableBigInteger mdivisor, int roundingMode,
                                         int qsign, MutableBigInteger mq, MutableBigInteger mr) {
        assert !mr.isZero();
        int cmpFracHalf = mr.compareHalf(mdivisor);
        return commonNeedIncrement(roundingMode, qsign, cmpFracHalf, mq.isOdd());
    }

    /**
     * Remove insignificant trailing zeros from this
     * {@code BigInteger} value until the preferred scale is reached or no
     * more zeros can be removed.  If the preferred scale is less than
     * Integer.MIN_VALUE, all the trailing zeros will be removed.
     *
     * @return new {@code BigDecimal} with a scale possibly reduced
     * to be closed to the preferred scale.
     */
    private static BigDecimal createAndStripZerosToMatchScale(BigInteger intVal, int scale, long preferredScale) {
        BigInteger qr[]; // quotient-remainder pair
        while (intVal.compareMagnitude(BigInteger.TEN) >= 0
               && scale > preferredScale) {
            if (intVal.testBit(0))
                break; // odd number cannot end in 0
            qr = intVal.divideAndRemainder(BigInteger.TEN);
            if (qr[1].signum() != 0)
                break; // non-0 remainder
            intVal = qr[0];
            scale = checkScale(intVal,(long) scale - 1); // could Overflow
        }
        return valueOf(intVal, scale, 0);
    }

    /**
     * Remove insignificant trailing zeros from this
     * {@code long} value until the preferred scale is reached or no
     * more zeros can be removed.  If the preferred scale is less than
     * Integer.MIN_VALUE, all the trailing zeros will be removed.
     *
     * @return new {@code BigDecimal} with a scale possibly reduced
     * to be closed to the preferred scale.
     */
    private static BigDecimal createAndStripZerosToMatchScale(long compactVal, int scale, long preferredScale) {
        while (Math.abs(compactVal) >= 10L && scale > preferredScale) {
            if ((compactVal & 1L) != 0L)
                break; // odd number cannot end in 0
            long r = compactVal % 10L;
            if (r != 0L)
                break; // non-0 remainder
            compactVal /= 10;
            scale = checkScale(compactVal, (long) scale - 1); // could Overflow
        }
        return valueOf(compactVal, scale);
    }

    private static BigDecimal stripZerosToMatchScale(BigInteger intVal, long intCompact, int scale, int preferredScale) {
        if(intCompact!=INFLATED) {
            return createAndStripZerosToMatchScale(intCompact, scale, preferredScale);
        } else {
            return createAndStripZerosToMatchScale(intVal==null ? INFLATED_BIGINT : intVal,
                                                   scale, preferredScale);
        }
    }

    /*
     * returns INFLATED if oveflow
     */
    private static long add(long xs, long ys){
        long sum = xs + ys;
        // See "Hacker's Delight" section 2-12 for explanation of
        // the overflow test.
        if ( (((sum ^ xs) & (sum ^ ys))) >= 0L) { // not overflowed
            return sum;
        }
        return INFLATED;
    }

    private static BigDecimal add(long xs, long ys, int scale){
        long sum = add(xs, ys);
        if (sum!=INFLATED)
            return BigDecimal.valueOf(sum, scale);
        return new BigDecimal(BigInteger.valueOf(xs).add(ys), scale);
    }

    private static BigDecimal add(final long xs, int scale1, final long ys, int scale2) {
        long sdiff = (long) scale1 - scale2;
        if (sdiff == 0) {
            return add(xs, ys, scale1);
        } else if (sdiff < 0) {
            int raise = checkScale(xs,-sdiff);
            long scaledX = longMultiplyPowerTen(xs, raise);
            if (scaledX != INFLATED) {
                return add(scaledX, ys, scale2);
            } else {
                BigInteger bigsum = bigMultiplyPowerTen(xs,raise).add(ys);
                return ((xs^ys)>=0) ? // same sign test
                    new BigDecimal(bigsum, INFLATED, scale2, 0)
                    : valueOf(bigsum, scale2, 0);
            }
        } else {
            int raise = checkScale(ys,sdiff);
            long scaledY = longMultiplyPowerTen(ys, raise);
            if (scaledY != INFLATED) {
                return add(xs, scaledY, scale1);
            } else {
                BigInteger bigsum = bigMultiplyPowerTen(ys,raise).add(xs);
                return ((xs^ys)>=0) ?
                    new BigDecimal(bigsum, INFLATED, scale1, 0)
                    : valueOf(bigsum, scale1, 0);
            }
        }
    }

    private static BigDecimal add(final long xs, int scale1, BigInteger snd, int scale2) {
        int rscale = scale1;
        long sdiff = (long)rscale - scale2;
        boolean sameSigns =  (Long.signum(xs) == snd.signum);
        BigInteger sum;
        if (sdiff < 0) {
            int raise = checkScale(xs,-sdiff);
            rscale = scale2;
            long scaledX = longMultiplyPowerTen(xs, raise);
            if (scaledX == INFLATED) {
                sum = snd.add(bigMultiplyPowerTen(xs,raise));
            } else {
                sum = snd.add(scaledX);
            }
        } else { //if (sdiff > 0) {
            int raise = checkScale(snd,sdiff);
            snd = bigMultiplyPowerTen(snd,raise);
            sum = snd.add(xs);
        }
        return (sameSigns) ?
            new BigDecimal(sum, INFLATED, rscale, 0) :
            valueOf(sum, rscale, 0);
    }

    private static BigDecimal add(BigInteger fst, int scale1, BigInteger snd, int scale2) {
        int rscale = scale1;
        long sdiff = (long)rscale - scale2;
        if (sdiff != 0) {
            if (sdiff < 0) {
                int raise = checkScale(fst,-sdiff);
                rscale = scale2;
                fst = bigMultiplyPowerTen(fst,raise);
            } else {
                int raise = checkScale(snd,sdiff);
                snd = bigMultiplyPowerTen(snd,raise);
            }
        }
        BigInteger sum = fst.add(snd);
        return (fst.signum == snd.signum) ?
                new BigDecimal(sum, INFLATED, rscale, 0) :
                valueOf(sum, rscale, 0);
    }

    private static BigInteger bigMultiplyPowerTen(long value, int n) {
        if (n <= 0)
            return BigInteger.valueOf(value);
        return bigTenToThe(n).multiply(value);
    }

    private static BigInteger bigMultiplyPowerTen(BigInteger value, int n) {
        if (n <= 0)
            return value;
        if(n<LONG_TEN_POWERS_TABLE.length) {
                return value.multiply(LONG_TEN_POWERS_TABLE[n]);
        }
        return value.multiply(bigTenToThe(n));
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (xs /
     * ys)}, with rounding according to the context settings.
     *
     * Fast path - used only when (xscale <= yscale && yscale < 18
     *  && mc.presision<18) {
     */
    private static BigDecimal divideSmallFastPath(final long xs, int xscale,
                                                  final long ys, int yscale,
                                                  long preferredScale, MathContext mc) {
        int mcp = mc.precision;
        int roundingMode = mc.roundingMode.oldMode;

        assert (xscale <= yscale) && (yscale < 18) && (mcp < 18);
        int xraise = yscale - xscale; // xraise >=0
        long scaledX = (xraise==0) ? xs :
            longMultiplyPowerTen(xs, xraise); // can't overflow here!
        BigDecimal quotient;

        int cmp = longCompareMagnitude(scaledX, ys);
        if(cmp > 0) { // satisfy constraint (b)
            yscale -= 1; // [that is, divisor *= 10]
            int scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp);
            if (checkScaleNonZero((long) mcp + yscale - xscale) > 0) {
                // assert newScale >= xscale
                int raise = checkScaleNonZero((long) mcp + yscale - xscale);
                long scaledXs;
                if ((scaledXs = longMultiplyPowerTen(xs, raise)) == INFLATED) {
                    quotient = null;
                    if((mcp-1) >=0 && (mcp-1)<LONG_TEN_POWERS_TABLE.length) {
                        quotient = multiplyDivideAndRound(LONG_TEN_POWERS_TABLE[mcp-1], scaledX, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
                    }
                    if(quotient==null) {
                        BigInteger rb = bigMultiplyPowerTen(scaledX,mcp-1);
                        quotient = divideAndRound(rb, ys,
                                                  scl, roundingMode, checkScaleNonZero(preferredScale));
                    }
                } else {
                    quotient = divideAndRound(scaledXs, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
                }
            } else {
                int newScale = checkScaleNonZero((long) xscale - mcp);
                // assert newScale >= yscale
                if (newScale == yscale) { // easy case
                    quotient = divideAndRound(xs, ys, scl, roundingMode,checkScaleNonZero(preferredScale));
                } else {
                    int raise = checkScaleNonZero((long) newScale - yscale);
                    long scaledYs;
                    if ((scaledYs = longMultiplyPowerTen(ys, raise)) == INFLATED) {
                        BigInteger rb = bigMultiplyPowerTen(ys,raise);
                        quotient = divideAndRound(BigInteger.valueOf(xs),
                                                  rb, scl, roundingMode,checkScaleNonZero(preferredScale));
                    } else {
                        quotient = divideAndRound(xs, scaledYs, scl, roundingMode,checkScaleNonZero(preferredScale));
                    }
                }
            }
        } else {
            // abs(scaledX) <= abs(ys)
            // result is "scaledX * 10^msp / ys"
            int scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp);
            if(cmp==0) {
                // abs(scaleX)== abs(ys) => result will be scaled 10^mcp + correct sign
                quotient = roundedTenPower(((scaledX < 0) == (ys < 0)) ? 1 : -1, mcp, scl, checkScaleNonZero(preferredScale));
            } else {
                // abs(scaledX) < abs(ys)
                long scaledXs;
                if ((scaledXs = longMultiplyPowerTen(scaledX, mcp)) == INFLATED) {
                    quotient = null;
                    if(mcp<LONG_TEN_POWERS_TABLE.length) {
                        quotient = multiplyDivideAndRound(LONG_TEN_POWERS_TABLE[mcp], scaledX, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
                    }
                    if(quotient==null) {
                        BigInteger rb = bigMultiplyPowerTen(scaledX,mcp);
                        quotient = divideAndRound(rb, ys,
                                                  scl, roundingMode, checkScaleNonZero(preferredScale));
                    }
                } else {
                    quotient = divideAndRound(scaledXs, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
                }
            }
        }
        // doRound, here, only affects 1000000000 case.
        return doRound(quotient,mc);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (xs /
     * ys)}, with rounding according to the context settings.
     */
    private static BigDecimal divide(final long xs, int xscale, final long ys, int yscale, long preferredScale, MathContext mc) {
        int mcp = mc.precision;
        if(xscale <= yscale && yscale < 18 && mcp<18) {
            return divideSmallFastPath(xs, xscale, ys, yscale, preferredScale, mc);
        }
        if (compareMagnitudeNormalized(xs, xscale, ys, yscale) > 0) {// satisfy constraint (b)
            yscale -= 1; // [that is, divisor *= 10]
        }
        int roundingMode = mc.roundingMode.oldMode;
        // In order to find out whether the divide generates the exact result,
        // we avoid calling the above divide method. 'quotient' holds the
        // return BigDecimal object whose scale will be set to 'scl'.
        int scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp);
        BigDecimal quotient;
        if (checkScaleNonZero((long) mcp + yscale - xscale) > 0) {
            int raise = checkScaleNonZero((long) mcp + yscale - xscale);
            long scaledXs;
            if ((scaledXs = longMultiplyPowerTen(xs, raise)) == INFLATED) {
                BigInteger rb = bigMultiplyPowerTen(xs,raise);
                quotient = divideAndRound(rb, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
            } else {
                quotient = divideAndRound(scaledXs, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
            }
        } else {
            int newScale = checkScaleNonZero((long) xscale - mcp);
            // assert newScale >= yscale
            if (newScale == yscale) { // easy case
                quotient = divideAndRound(xs, ys, scl, roundingMode,checkScaleNonZero(preferredScale));
            } else {
                int raise = checkScaleNonZero((long) newScale - yscale);
                long scaledYs;
                if ((scaledYs = longMultiplyPowerTen(ys, raise)) == INFLATED) {
                    BigInteger rb = bigMultiplyPowerTen(ys,raise);
                    quotient = divideAndRound(BigInteger.valueOf(xs),
                                              rb, scl, roundingMode,checkScaleNonZero(preferredScale));
                } else {
                    quotient = divideAndRound(xs, scaledYs, scl, roundingMode,checkScaleNonZero(preferredScale));
                }
            }
        }
        // doRound, here, only affects 1000000000 case.
        return doRound(quotient,mc);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (xs /
     * ys)}, with rounding according to the context settings.
     */
    private static BigDecimal divide(BigInteger xs, int xscale, long ys, int yscale, long preferredScale, MathContext mc) {
        // Normalize dividend & divisor so that both fall into [0.1, 0.999...]
        if ((-compareMagnitudeNormalized(ys, yscale, xs, xscale)) > 0) {// satisfy constraint (b)
            yscale -= 1; // [that is, divisor *= 10]
        }
        int mcp = mc.precision;
        int roundingMode = mc.roundingMode.oldMode;

        // In order to find out whether the divide generates the exact result,
        // we avoid calling the above divide method. 'quotient' holds the
        // return BigDecimal object whose scale will be set to 'scl'.
        BigDecimal quotient;
        int scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp);
        if (checkScaleNonZero((long) mcp + yscale - xscale) > 0) {
            int raise = checkScaleNonZero((long) mcp + yscale - xscale);
            BigInteger rb = bigMultiplyPowerTen(xs,raise);
            quotient = divideAndRound(rb, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
        } else {
            int newScale = checkScaleNonZero((long) xscale - mcp);
            // assert newScale >= yscale
            if (newScale == yscale) { // easy case
                quotient = divideAndRound(xs, ys, scl, roundingMode,checkScaleNonZero(preferredScale));
            } else {
                int raise = checkScaleNonZero((long) newScale - yscale);
                long scaledYs;
                if ((scaledYs = longMultiplyPowerTen(ys, raise)) == INFLATED) {
                    BigInteger rb = bigMultiplyPowerTen(ys,raise);
                    quotient = divideAndRound(xs, rb, scl, roundingMode,checkScaleNonZero(preferredScale));
                } else {
                    quotient = divideAndRound(xs, scaledYs, scl, roundingMode,checkScaleNonZero(preferredScale));
                }
            }
        }
        // doRound, here, only affects 1000000000 case.
        return doRound(quotient, mc);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (xs /
     * ys)}, with rounding according to the context settings.
     */
    private static BigDecimal divide(long xs, int xscale, BigInteger ys, int yscale, long preferredScale, MathContext mc) {
        // Normalize dividend & divisor so that both fall into [0.1, 0.999...]
        if (compareMagnitudeNormalized(xs, xscale, ys, yscale) > 0) {// satisfy constraint (b)
            yscale -= 1; // [that is, divisor *= 10]
        }
        int mcp = mc.precision;
        int roundingMode = mc.roundingMode.oldMode;

        // In order to find out whether the divide generates the exact result,
        // we avoid calling the above divide method. 'quotient' holds the
        // return BigDecimal object whose scale will be set to 'scl'.
        BigDecimal quotient;
        int scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp);
        if (checkScaleNonZero((long) mcp + yscale - xscale) > 0) {
            int raise = checkScaleNonZero((long) mcp + yscale - xscale);
            BigInteger rb = bigMultiplyPowerTen(xs,raise);
            quotient = divideAndRound(rb, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
        } else {
            int newScale = checkScaleNonZero((long) xscale - mcp);
            int raise = checkScaleNonZero((long) newScale - yscale);
            BigInteger rb = bigMultiplyPowerTen(ys,raise);
            quotient = divideAndRound(BigInteger.valueOf(xs), rb, scl, roundingMode,checkScaleNonZero(preferredScale));
        }
        // doRound, here, only affects 1000000000 case.
        return doRound(quotient, mc);
    }

    /**
     * Returns a {@code BigDecimal} whose value is {@code (xs /
     * ys)}, with rounding according to the context settings.
     */
    private static BigDecimal divide(BigInteger xs, int xscale, BigInteger ys, int yscale, long preferredScale, MathContext mc) {
        // Normalize dividend & divisor so that both fall into [0.1, 0.999...]
        if (compareMagnitudeNormalized(xs, xscale, ys, yscale) > 0) {// satisfy constraint (b)
            yscale -= 1; // [that is, divisor *= 10]
        }
        int mcp = mc.precision;
        int roundingMode = mc.roundingMode.oldMode;

        // In order to find out whether the divide generates the exact result,
        // we avoid calling the above divide method. 'quotient' holds the
        // return BigDecimal object whose scale will be set to 'scl'.
        BigDecimal quotient;
        int scl = checkScaleNonZero(preferredScale + yscale - xscale + mcp);
        if (checkScaleNonZero((long) mcp + yscale - xscale) > 0) {
            int raise = checkScaleNonZero((long) mcp + yscale - xscale);
            BigInteger rb = bigMultiplyPowerTen(xs,raise);
            quotient = divideAndRound(rb, ys, scl, roundingMode, checkScaleNonZero(preferredScale));
        } else {
            int newScale = checkScaleNonZero((long) xscale - mcp);
            int raise = checkScaleNonZero((long) newScale - yscale);
            BigInteger rb = bigMultiplyPowerTen(ys,raise);
            quotient = divideAndRound(xs, rb, scl, roundingMode,checkScaleNonZero(preferredScale));
        }
        // doRound, here, only affects 1000000000 case.
        return doRound(quotient, mc);
    }

    /*
     * performs divideAndRound for (dividend0*dividend1, divisor)
     * returns null if quotient can't fit into long value;
     */
    private static BigDecimal multiplyDivideAndRound(long dividend0, long dividend1, long divisor, int scale, int roundingMode,
                                                     int preferredScale) {
        int qsign = Long.signum(dividend0)*Long.signum(dividend1)*Long.signum(divisor);
        dividend0 = Math.abs(dividend0);
        dividend1 = Math.abs(dividend1);
        divisor = Math.abs(divisor);
        // multiply dividend0 * dividend1
        long d0_hi = dividend0 >>> 32;
        long d0_lo = dividend0 & LONG_MASK;
        long d1_hi = dividend1 >>> 32;
        long d1_lo = dividend1 & LONG_MASK;
        long product = d0_lo * d1_lo;
        long d0 = product & LONG_MASK;
        long d1 = product >>> 32;
        product = d0_hi * d1_lo + d1;
        d1 = product & LONG_MASK;
        long d2 = product >>> 32;
        product = d0_lo * d1_hi + d1;
        d1 = product & LONG_MASK;
        d2 += product >>> 32;
        long d3 = d2>>>32;
        d2 &= LONG_MASK;
        product = d0_hi*d1_hi + d2;
        d2 = product & LONG_MASK;
        d3 = ((product>>>32) + d3) & LONG_MASK;
        final long dividendHi = make64(d3,d2);
        final long dividendLo = make64(d1,d0);
        // divide
        return divideAndRound128(dividendHi, dividendLo, divisor, qsign, scale, roundingMode, preferredScale);
    }

    private static final long DIV_NUM_BASE = (1L<<32); // Number base (32 bits).

    /*
     * divideAndRound 128-bit value by long divisor.
     * returns null if quotient can't fit into long value;
     * Specialized version of Knuth's division
     */
    private static BigDecimal divideAndRound128(final long dividendHi, final long dividendLo, long divisor, int sign,
                                                int scale, int roundingMode, int preferredScale) {
        if (dividendHi >= divisor) {
            return null;
        }

        final int shift = Long.numberOfLeadingZeros(divisor);
        divisor <<= shift;

        final long v1 = divisor >>> 32;
        final long v0 = divisor & LONG_MASK;

        long tmp = dividendLo << shift;
        long u1 = tmp >>> 32;
        long u0 = tmp & LONG_MASK;

        tmp = (dividendHi << shift) | (dividendLo >>> 64 - shift);
        long u2 = tmp & LONG_MASK;
        long q1, r_tmp;
        if (v1 == 1) {
            q1 = tmp;
            r_tmp = 0;
        } else if (tmp >= 0) {
            q1 = tmp / v1;
            r_tmp = tmp - q1 * v1;
        } else {
            long[] rq = divRemNegativeLong(tmp, v1);
            q1 = rq[1];
            r_tmp = rq[0];
        }

        while(q1 >= DIV_NUM_BASE || unsignedLongCompare(q1*v0, make64(r_tmp, u1))) {
            q1--;
            r_tmp += v1;
            if (r_tmp >= DIV_NUM_BASE)
                break;
        }

        tmp = mulsub(u2,u1,v1,v0,q1);
        u1 = tmp & LONG_MASK;
        long q0;
        if (v1 == 1) {
            q0 = tmp;
            r_tmp = 0;
        } else if (tmp >= 0) {
            q0 = tmp / v1;
            r_tmp = tmp - q0 * v1;
        } else {
            long[] rq = divRemNegativeLong(tmp, v1);
            q0 = rq[1];
            r_tmp = rq[0];
        }

        while(q0 >= DIV_NUM_BASE || unsignedLongCompare(q0*v0,make64(r_tmp,u0))) {
            q0--;
            r_tmp += v1;
            if (r_tmp >= DIV_NUM_BASE)
                break;
        }

        if((int)q1 < 0) {
            // result (which is positive and unsigned here)
            // can't fit into long due to sign bit is used for value
            MutableBigInteger mq = new MutableBigInteger(new int[]{(int)q1, (int)q0});
            if (roundingMode == ROUND_DOWN && scale == preferredScale) {
                return mq.toBigDecimal(sign, scale);
            }
            long r = mulsub(u1, u0, v1, v0, q0) >>> shift;
            if (r != 0) {
                if(needIncrement(divisor >>> shift, roundingMode, sign, mq, r)){
                    mq.add(MutableBigInteger.ONE);
                }
                return mq.toBigDecimal(sign, scale);
            } else {
                if (preferredScale != scale) {
                    BigInteger intVal =  mq.toBigInteger(sign);
                    return createAndStripZerosToMatchScale(intVal,scale, preferredScale);
                } else {
                    return mq.toBigDecimal(sign, scale);
                }
            }
        }

        long q = make64(q1,q0);
        q*=sign;

        if (roundingMode == ROUND_DOWN && scale == preferredScale)
            return valueOf(q, scale);

        long r = mulsub(u1, u0, v1, v0, q0) >>> shift;
        if (r != 0) {
            boolean increment = needIncrement(divisor >>> shift, roundingMode, sign, q, r);
            return valueOf((increment ? q + sign : q), scale);
        } else {
            if (preferredScale != scale) {
                return createAndStripZerosToMatchScale(q, scale, preferredScale);
            } else {
                return valueOf(q, scale);
            }
        }
    }

    /*
     * calculate divideAndRound for ldividend*10^raise / divisor
     * when abs(dividend)==abs(divisor);
     */
    private static BigDecimal roundedTenPower(int qsign, int raise, int scale, int preferredScale) {
        if (scale > preferredScale) {
            int diff = scale - preferredScale;
            if(diff < raise) {
                return scaledTenPow(raise - diff, qsign, preferredScale);
            } else {
                return valueOf(qsign,scale-raise);
            }
        } else {
            return scaledTenPow(raise, qsign, scale);
        }
    }

    static BigDecimal scaledTenPow(int n, int sign, int scale) {
        if (n < LONG_TEN_POWERS_TABLE.length)
            return valueOf(sign*LONG_TEN_POWERS_TABLE[n],scale);
        else {
            BigInteger unscaledVal = bigTenToThe(n);
            if(sign==-1) {
                unscaledVal = unscaledVal.negate();
            }
            return new BigDecimal(unscaledVal, INFLATED, scale, n+1);
        }
    }

    /**
     * Calculate the quotient and remainder of dividing a negative long by
     * another long.
     *
     * @param n the numerator; must be negative
     * @param d the denominator; must not be unity
     * @return a two-element {@long} array with the remainder and quotient in
     *         the initial and final elements, respectively
     */
    private static long[] divRemNegativeLong(long n, long d) {
        assert n < 0 : "Non-negative numerator " + n;
        assert d != 1 : "Unity denominator";

        // Approximate the quotient and remainder
        long q = (n >>> 1) / (d >>> 1);
        long r = n - q * d;

        // Correct the approximation
        while (r < 0) {
            r += d;
            q--;
        }
        while (r >= d) {
            r -= d;
            q++;
        }

        // n - q*d == r && 0 <= r < d, hence we're done.
        return new long[] {r, q};
    }

    private static long make64(long hi, long lo) {
        return hi<<32 | lo;
    }

    private static long mulsub(long u1, long u0, final long v1, final long v0, long q0) {
        long tmp = u0 - q0*v0;
        return make64(u1 + (tmp>>>32) - q0*v1,tmp & LONG_MASK);
    }

    private static boolean unsignedLongCompare(long one, long two) {
        return (one+Long.MIN_VALUE) > (two+Long.MIN_VALUE);
    }

    private static boolean unsignedLongCompareEq(long one, long two) {
        return (one+Long.MIN_VALUE) >= (two+Long.MIN_VALUE);
    }


    // Compare Normalize dividend & divisor so that both fall into [0.1, 0.999...]
    private static int compareMagnitudeNormalized(long xs, int xscale, long ys, int yscale) {
        // assert xs!=0 && ys!=0
        int sdiff = xscale - yscale;
        if (sdiff != 0) {
            if (sdiff < 0) {
                xs = longMultiplyPowerTen(xs, -sdiff);
            } else { // sdiff > 0
                ys = longMultiplyPowerTen(ys, sdiff);
            }
        }
        if (xs != INFLATED)
            return (ys != INFLATED) ? longCompareMagnitude(xs, ys) : -1;
        else
            return 1;
    }

    // Compare Normalize dividend & divisor so that both fall into [0.1, 0.999...]
    private static int compareMagnitudeNormalized(long xs, int xscale, BigInteger ys, int yscale) {
        // assert "ys can't be represented as long"
        if (xs == 0)
            return -1;
        int sdiff = xscale - yscale;
        if (sdiff < 0) {
            if (longMultiplyPowerTen(xs, -sdiff) == INFLATED ) {
                return bigMultiplyPowerTen(xs, -sdiff).compareMagnitude(ys);
            }
        }
        return -1;
    }

    // Compare Normalize dividend & divisor so that both fall into [0.1, 0.999...]
    private static int compareMagnitudeNormalized(BigInteger xs, int xscale, BigInteger ys, int yscale) {
        int sdiff = xscale - yscale;
        if (sdiff < 0) {
            return bigMultiplyPowerTen(xs, -sdiff).compareMagnitude(ys);
        } else { // sdiff >= 0
            return xs.compareMagnitude(bigMultiplyPowerTen(ys, sdiff));
        }
    }

    private static long multiply(long x, long y){
                long product = x * y;
        long ax = Math.abs(x);
        long ay = Math.abs(y);
        if (((ax | ay) >>> 31 == 0) || (y == 0) || (product / y == x)){
                        return product;
                }
        return INFLATED;
    }

    private static BigDecimal multiply(long x, long y, int scale) {
        long product = multiply(x, y);
        if(product!=INFLATED) {
            return valueOf(product,scale);
        }
        return new BigDecimal(BigInteger.valueOf(x).multiply(y),INFLATED,scale,0);
    }

    private static BigDecimal multiply(long x, BigInteger y, int scale) {
        if(x==0) {
            return zeroValueOf(scale);
        }
        return new BigDecimal(y.multiply(x),INFLATED,scale,0);
    }

    private static BigDecimal multiply(BigInteger x, BigInteger y, int scale) {
        return new BigDecimal(x.multiply(y),INFLATED,scale,0);
    }

    /**
     * Multiplies two long values and rounds according {@code MathContext}
     */
    private static BigDecimal multiplyAndRound(long x, long y, int scale, MathContext mc) {
        long product = multiply(x, y);
        if(product!=INFLATED) {
            return doRound(product, scale, mc);
        }
        // attempt to do it in 128 bits
        int rsign = 1;
        if(x < 0) {
            x = -x;
            rsign = -1;
        }
        if(y < 0) {
            y = -y;
            rsign *= -1;
        }
        // multiply dividend0 * dividend1
        long m0_hi = x >>> 32;
        long m0_lo = x & LONG_MASK;
        long m1_hi = y >>> 32;
        long m1_lo = y & LONG_MASK;
        product = m0_lo * m1_lo;
        long m0 = product & LONG_MASK;
        long m1 = product >>> 32;
        product = m0_hi * m1_lo + m1;
        m1 = product & LONG_MASK;
        long m2 = product >>> 32;
        product = m0_lo * m1_hi + m1;
        m1 = product & LONG_MASK;
        m2 += product >>> 32;
        long m3 = m2>>>32;
        m2 &= LONG_MASK;
        product = m0_hi*m1_hi + m2;
        m2 = product & LONG_MASK;
        m3 = ((product>>>32) + m3) & LONG_MASK;
        final long mHi = make64(m3,m2);
        final long mLo = make64(m1,m0);
        BigDecimal res = doRound128(mHi, mLo, rsign, scale, mc);
        if(res!=null) {
            return res;
        }
        res = new BigDecimal(BigInteger.valueOf(x).multiply(y*rsign), INFLATED, scale, 0);
        return doRound(res,mc);
    }

    private static BigDecimal multiplyAndRound(long x, BigInteger y, int scale, MathContext mc) {
        if(x==0) {
            return zeroValueOf(scale);
        }
        return doRound(y.multiply(x), scale, mc);
    }

    private static BigDecimal multiplyAndRound(BigInteger x, BigInteger y, int scale, MathContext mc) {
        return doRound(x.multiply(y), scale, mc);
    }

    /**
     * rounds 128-bit value according {@code MathContext}
     * returns null if result can't be repsented as compact BigDecimal.
     */
    private static BigDecimal doRound128(long hi, long lo, int sign, int scale, MathContext mc) {
        int mcp = mc.precision;
        int drop;
        BigDecimal res = null;
        if(((drop = precision(hi, lo) - mcp) > 0)&&(drop<LONG_TEN_POWERS_TABLE.length)) {
            scale = checkScaleNonZero((long)scale - drop);
            res = divideAndRound128(hi, lo, LONG_TEN_POWERS_TABLE[drop], sign, scale, mc.roundingMode.oldMode, scale);
        }
        if(res!=null) {
            return doRound(res,mc);
        }
        return null;
    }

    private static final long[][] LONGLONG_TEN_POWERS_TABLE = {
        {   0L, 0x8AC7230489E80000L },  //10^19
        {       0x5L, 0x6bc75e2d63100000L },  //10^20
        {       0x36L, 0x35c9adc5dea00000L },  //10^21
        {       0x21eL, 0x19e0c9bab2400000L  },  //10^22
        {       0x152dL, 0x02c7e14af6800000L  },  //10^23
        {       0xd3c2L, 0x1bcecceda1000000L  },  //10^24
        {       0x84595L, 0x161401484a000000L  },  //10^25
        {       0x52b7d2L, 0xdcc80cd2e4000000L  },  //10^26
        {       0x33b2e3cL, 0x9fd0803ce8000000L  },  //10^27
        {       0x204fce5eL, 0x3e25026110000000L  },  //10^28
        {       0x1431e0faeL, 0x6d7217caa0000000L  },  //10^29
        {       0xc9f2c9cd0L, 0x4674edea40000000L  },  //10^30
        {       0x7e37be2022L, 0xc0914b2680000000L  },  //10^31
        {       0x4ee2d6d415bL, 0x85acef8100000000L  },  //10^32
        {       0x314dc6448d93L, 0x38c15b0a00000000L  },  //10^33
        {       0x1ed09bead87c0L, 0x378d8e6400000000L  },  //10^34
        {       0x13426172c74d82L, 0x2b878fe800000000L  },  //10^35
        {       0xc097ce7bc90715L, 0xb34b9f1000000000L  },  //10^36
        {       0x785ee10d5da46d9L, 0x00f436a000000000L  },  //10^37
        {       0x4b3b4ca85a86c47aL, 0x098a224000000000L  },  //10^38
    };

    /*
     * returns precision of 128-bit value
     */
    private static int precision(long hi, long lo){
        if(hi==0) {
            if(lo>=0) {
                return longDigitLength(lo);
            }
            return (unsignedLongCompareEq(lo, LONGLONG_TEN_POWERS_TABLE[0][1])) ? 20 : 19;
            // 0x8AC7230489E80000L  = unsigned 2^19
        }
        int r = ((128 - Long.numberOfLeadingZeros(hi) + 1) * 1233) >>> 12;
        int idx = r-19;
        return (idx >= LONGLONG_TEN_POWERS_TABLE.length || longLongCompareMagnitude(hi, lo,
                                                                                    LONGLONG_TEN_POWERS_TABLE[idx][0], LONGLONG_TEN_POWERS_TABLE[idx][1])) ? r : r + 1;
    }

    /*
     * returns true if 128 bit number <hi0,lo0> is less then <hi1,lo1>
     * hi0 & hi1 should be non-negative
     */
    private static boolean longLongCompareMagnitude(long hi0, long lo0, long hi1, long lo1) {
        if(hi0!=hi1) {
            return hi0<hi1;
        }
        return (lo0+Long.MIN_VALUE) <(lo1+Long.MIN_VALUE);
    }

    private static BigDecimal divide(long dividend, int dividendScale, long divisor, int divisorScale, int scale, int roundingMode) {
        if (checkScale(dividend,(long)scale + divisorScale) > dividendScale) {
            int newScale = scale + divisorScale;
            int raise = newScale - dividendScale;
            if(raise<LONG_TEN_POWERS_TABLE.length) {
                long xs = dividend;
                if ((xs = longMultiplyPowerTen(xs, raise)) != INFLATED) {
                    return divideAndRound(xs, divisor, scale, roundingMode, scale);
                }
                BigDecimal q = multiplyDivideAndRound(LONG_TEN_POWERS_TABLE[raise], dividend, divisor, scale, roundingMode, scale);
                if(q!=null) {
                    return q;
                }
            }
            BigInteger scaledDividend = bigMultiplyPowerTen(dividend, raise);
            return divideAndRound(scaledDividend, divisor, scale, roundingMode, scale);
        } else {
            int newScale = checkScale(divisor,(long)dividendScale - scale);
            int raise = newScale - divisorScale;
            if(raise<LONG_TEN_POWERS_TABLE.length) {
                long ys = divisor;
                if ((ys = longMultiplyPowerTen(ys, raise)) != INFLATED) {
                    return divideAndRound(dividend, ys, scale, roundingMode, scale);
                }
            }
            BigInteger scaledDivisor = bigMultiplyPowerTen(divisor, raise);
            return divideAndRound(BigInteger.valueOf(dividend), scaledDivisor, scale, roundingMode, scale);
        }
    }

    private static BigDecimal divide(BigInteger dividend, int dividendScale, long divisor, int divisorScale, int scale, int roundingMode) {
        if (checkScale(dividend,(long)scale + divisorScale) > dividendScale) {
            int newScale = scale + divisorScale;
            int raise = newScale - dividendScale;
            BigInteger scaledDividend = bigMultiplyPowerTen(dividend, raise);
            return divideAndRound(scaledDividend, divisor, scale, roundingMode, scale);
        } else {
            int newScale = checkScale(divisor,(long)dividendScale - scale);
            int raise = newScale - divisorScale;
            if(raise<LONG_TEN_POWERS_TABLE.length) {
                long ys = divisor;
                if ((ys = longMultiplyPowerTen(ys, raise)) != INFLATED) {
                    return divideAndRound(dividend, ys, scale, roundingMode, scale);
                }
            }
            BigInteger scaledDivisor = bigMultiplyPowerTen(divisor, raise);
            return divideAndRound(dividend, scaledDivisor, scale, roundingMode, scale);
        }
    }

    private static BigDecimal divide(long dividend, int dividendScale, BigInteger divisor, int divisorScale, int scale, int roundingMode) {
        if (checkScale(dividend,(long)scale + divisorScale) > dividendScale) {
            int newScale = scale + divisorScale;
            int raise = newScale - dividendScale;
            BigInteger scaledDividend = bigMultiplyPowerTen(dividend, raise);
            return divideAndRound(scaledDividend, divisor, scale, roundingMode, scale);
        } else {
            int newScale = checkScale(divisor,(long)dividendScale - scale);
            int raise = newScale - divisorScale;
            BigInteger scaledDivisor = bigMultiplyPowerTen(divisor, raise);
            return divideAndRound(BigInteger.valueOf(dividend), scaledDivisor, scale, roundingMode, scale);
        }
    }

    private static BigDecimal divide(BigInteger dividend, int dividendScale, BigInteger divisor, int divisorScale, int scale, int roundingMode) {
        if (checkScale(dividend,(long)scale + divisorScale) > dividendScale) {
            int newScale = scale + divisorScale;
            int raise = newScale - dividendScale;
            BigInteger scaledDividend = bigMultiplyPowerTen(dividend, raise);
            return divideAndRound(scaledDividend, divisor, scale, roundingMode, scale);
        } else {
            int newScale = checkScale(divisor,(long)dividendScale - scale);
            int raise = newScale - divisorScale;
            BigInteger scaledDivisor = bigMultiplyPowerTen(divisor, raise);
            return divideAndRound(dividend, scaledDivisor, scale, roundingMode, scale);
        }
    }

}
/*
 * Copyright (c) 1996, 2018, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 * Portions Copyright (c) 1995  Colin Plumb.  All rights reserved.
 */

package java.math;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.ObjectStreamException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import sun.misc.DoubleConsts;
import sun.misc.FloatConsts;

/**
 * Immutable arbitrary-precision integers.  All operations behave as if
 * BigIntegers were represented in two's-complement notation (like Java's
 * primitive integer types).  BigInteger provides analogues to all of Java's
 * primitive integer operators, and all relevant methods from java.lang.Math.
 * Additionally, BigInteger provides operations for modular arithmetic, GCD
 * calculation, primality testing, prime generation, bit manipulation,
 * and a few other miscellaneous operations.
 *
 * <p>Semantics of arithmetic operations exactly mimic those of Java's integer
 * arithmetic operators, as defined in <i>The Java Language Specification</i>.
 * For example, division by zero throws an {@code ArithmeticException}, and
 * division of a negative by a positive yields a negative (or zero) remainder.
 * All of the details in the Spec concerning overflow are ignored, as
 * BigIntegers are made as large as necessary to accommodate the results of an
 * operation.
 *
 * <p>Semantics of shift operations extend those of Java's shift operators
 * to allow for negative shift distances.  A right-shift with a negative
 * shift distance results in a left shift, and vice-versa.  The unsigned
 * right shift operator ({@code >>>}) is omitted, as this operation makes
 * little sense in combination with the "infinite word size" abstraction
 * provided by this class.
 *
 * <p>Semantics of bitwise logical operations exactly mimic those of Java's
 * bitwise integer operators.  The binary operators ({@code and},
 * {@code or}, {@code xor}) implicitly perform sign extension on the shorter
 * of the two operands prior to performing the operation.
 *
 * <p>Comparison operations perform signed integer comparisons, analogous to
 * those performed by Java's relational and equality operators.
 *
 * <p>Modular arithmetic operations are provided to compute residues, perform
 * exponentiation, and compute multiplicative inverses.  These methods always
 * return a non-negative result, between {@code 0} and {@code (modulus - 1)},
 * inclusive.
 *
 * <p>Bit operations operate on a single bit of the two's-complement
 * representation of their operand.  If necessary, the operand is sign-
 * extended so that it contains the designated bit.  None of the single-bit
 * operations can produce a BigInteger with a different sign from the
 * BigInteger being operated on, as they affect only a single bit, and the
 * "infinite word size" abstraction provided by this class ensures that there
 * are infinitely many "virtual sign bits" preceding each BigInteger.
 *
 * <p>For the sake of brevity and clarity, pseudo-code is used throughout the
 * descriptions of BigInteger methods.  The pseudo-code expression
 * {@code (i + j)} is shorthand for "a BigInteger whose value is
 * that of the BigInteger {@code i} plus that of the BigInteger {@code j}."
 * The pseudo-code expression {@code (i == j)} is shorthand for
 * "{@code true} if and only if the BigInteger {@code i} represents the same
 * value as the BigInteger {@code j}."  Other pseudo-code expressions are
 * interpreted similarly.
 *
 * <p>All methods and constructors in this class throw
 * {@code NullPointerException} when passed
 * a null object reference for any input parameter.
 *
 * BigInteger must support values in the range
 * -2<sup>{@code Integer.MAX_VALUE}</sup> (exclusive) to
 * +2<sup>{@code Integer.MAX_VALUE}</sup> (exclusive)
 * and may support values outside of that range.
 *
 * The range of probable prime values is limited and may be less than
 * the full supported positive range of {@code BigInteger}.
 * The range must be at least 1 to 2<sup>500000000</sup>.
 *
 * @implNote
 * BigInteger constructors and operations throw {@code ArithmeticException} when
 * the result is out of the supported range of
 * -2<sup>{@code Integer.MAX_VALUE}</sup> (exclusive) to
 * +2<sup>{@code Integer.MAX_VALUE}</sup> (exclusive).
 *
 * @see     BigDecimal
 * @author  Josh Bloch
 * @author  Michael McCloskey
 * @author  Alan Eliasen
 * @author  Timothy Buktu
 * @since JDK1.1
 */

public class BigInteger extends Number implements Comparable<BigInteger> {
    /**
     * The signum of this BigInteger: -1 for negative, 0 for zero, or
     * 1 for positive.  Note that the BigInteger zero <i>must</i> have
     * a signum of 0.  This is necessary to ensures that there is exactly one
     * representation for each BigInteger value.
     *
     * @serial
     */
    final int signum;

    /**
     * The magnitude of this BigInteger, in <i>big-endian</i> order: the
     * zeroth element of this array is the most-significant int of the
     * magnitude.  The magnitude must be "minimal" in that the most-significant
     * int ({@code mag[0]}) must be non-zero.  This is necessary to
     * ensure that there is exactly one representation for each BigInteger
     * value.  Note that this implies that the BigInteger zero has a
     * zero-length mag array.
     */
    final int[] mag;

    // These "redundant fields" are initialized with recognizable nonsense
    // values, and cached the first time they are needed (or never, if they
    // aren't needed).

     /**
     * One plus the bitCount of this BigInteger. Zeros means unitialized.
     *
     * @serial
     * @see #bitCount
     * @deprecated Deprecated since logical value is offset from stored
     * value and correction factor is applied in accessor method.
     */
    @Deprecated
    private int bitCount;

    /**
     * One plus the bitLength of this BigInteger. Zeros means unitialized.
     * (either value is acceptable).
     *
     * @serial
     * @see #bitLength()
     * @deprecated Deprecated since logical value is offset from stored
     * value and correction factor is applied in accessor method.
     */
    @Deprecated
    private int bitLength;

    /**
     * Two plus the lowest set bit of this BigInteger, as returned by
     * getLowestSetBit().
     *
     * @serial
     * @see #getLowestSetBit
     * @deprecated Deprecated since logical value is offset from stored
     * value and correction factor is applied in accessor method.
     */
    @Deprecated
    private int lowestSetBit;

    /**
     * Two plus the index of the lowest-order int in the magnitude of this
     * BigInteger that contains a nonzero int, or -2 (either value is acceptable).
     * The least significant int has int-number 0, the next int in order of
     * increasing significance has int-number 1, and so forth.
     * @deprecated Deprecated since logical value is offset from stored
     * value and correction factor is applied in accessor method.
     */
    @Deprecated
    private int firstNonzeroIntNum;

    /**
     * This mask is used to obtain the value of an int as if it were unsigned.
     */
    final static long LONG_MASK = 0xffffffffL;

    /**
     * This constant limits {@code mag.length} of BigIntegers to the supported
     * range.
     */
    private static final int MAX_MAG_LENGTH = Integer.MAX_VALUE / Integer.SIZE + 1; // (1 << 26)

    /**
     * Bit lengths larger than this constant can cause overflow in searchLen
     * calculation and in BitSieve.singleSearch method.
     */
    private static final  int PRIME_SEARCH_BIT_LENGTH_LIMIT = 500000000;

    /**
     * The threshold value for using Karatsuba multiplication.  If the number
     * of ints in both mag arrays are greater than this number, then
     * Karatsuba multiplication will be used.   This value is found
     * experimentally to work well.
     */
    private static final int KARATSUBA_THRESHOLD = 80;

    /**
     * The threshold value for using 3-way Toom-Cook multiplication.
     * If the number of ints in each mag array is greater than the
     * Karatsuba threshold, and the number of ints in at least one of
     * the mag arrays is greater than this threshold, then Toom-Cook
     * multiplication will be used.
     */
    private static final int TOOM_COOK_THRESHOLD = 240;

    /**
     * The threshold value for using Karatsuba squaring.  If the number
     * of ints in the number are larger than this value,
     * Karatsuba squaring will be used.   This value is found
     * experimentally to work well.
     */
    private static final int KARATSUBA_SQUARE_THRESHOLD = 128;

    /**
     * The threshold value for using Toom-Cook squaring.  If the number
     * of ints in the number are larger than this value,
     * Toom-Cook squaring will be used.   This value is found
     * experimentally to work well.
     */
    private static final int TOOM_COOK_SQUARE_THRESHOLD = 216;

    /**
     * The threshold value for using Burnikel-Ziegler division.  If the number
     * of ints in the divisor are larger than this value, Burnikel-Ziegler
     * division may be used.  This value is found experimentally to work well.
     */
    static final int BURNIKEL_ZIEGLER_THRESHOLD = 80;

    /**
     * The offset value for using Burnikel-Ziegler division.  If the number
     * of ints in the divisor exceeds the Burnikel-Ziegler threshold, and the
     * number of ints in the dividend is greater than the number of ints in the
     * divisor plus this value, Burnikel-Ziegler division will be used.  This
     * value is found experimentally to work well.
     */
    static final int BURNIKEL_ZIEGLER_OFFSET = 40;

    /**
     * The threshold value for using Schoenhage recursive base conversion. If
     * the number of ints in the number are larger than this value,
     * the Schoenhage algorithm will be used.  In practice, it appears that the
     * Schoenhage routine is faster for any threshold down to 2, and is
     * relatively flat for thresholds between 2-25, so this choice may be
     * varied within this range for very small effect.
     */
    private static final int SCHOENHAGE_BASE_CONVERSION_THRESHOLD = 20;

    /**
     * The threshold value for using squaring code to perform multiplication
     * of a {@code BigInteger} instance by itself.  If the number of ints in
     * the number are larger than this value, {@code multiply(this)} will
     * return {@code square()}.
     */
    private static final int MULTIPLY_SQUARE_THRESHOLD = 20;

    /**
     * The threshold for using an intrinsic version of
     * implMontgomeryXXX to perform Montgomery multiplication.  If the
     * number of ints in the number is more than this value we do not
     * use the intrinsic.
     */
    private static final int MONTGOMERY_INTRINSIC_THRESHOLD = 512;


    // Constructors

    /**
     * Translates a byte array containing the two's-complement binary
     * representation of a BigInteger into a BigInteger.  The input array is
     * assumed to be in <i>big-endian</i> byte-order: the most significant
     * byte is in the zeroth element.
     *
     * @param  val big-endian two's-complement binary representation of
     *         BigInteger.
     * @throws NumberFormatException {@code val} is zero bytes long.
     */
    public BigInteger(byte[] val) {
        if (val.length == 0)
            throw new NumberFormatException("Zero length BigInteger");

        if (val[0] < 0) {
            mag = makePositive(val);
            signum = -1;
        } else {
            mag = stripLeadingZeroBytes(val);
            signum = (mag.length == 0 ? 0 : 1);
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * This private constructor translates an int array containing the
     * two's-complement binary representation of a BigInteger into a
     * BigInteger. The input array is assumed to be in <i>big-endian</i>
     * int-order: the most significant int is in the zeroth element.
     */
    private BigInteger(int[] val) {
        if (val.length == 0)
            throw new NumberFormatException("Zero length BigInteger");

        if (val[0] < 0) {
            mag = makePositive(val);
            signum = -1;
        } else {
            mag = trustedStripLeadingZeroInts(val);
            signum = (mag.length == 0 ? 0 : 1);
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * Translates the sign-magnitude representation of a BigInteger into a
     * BigInteger.  The sign is represented as an integer signum value: -1 for
     * negative, 0 for zero, or 1 for positive.  The magnitude is a byte array
     * in <i>big-endian</i> byte-order: the most significant byte is in the
     * zeroth element.  A zero-length magnitude array is permissible, and will
     * result in a BigInteger value of 0, whether signum is -1, 0 or 1.
     *
     * @param  signum signum of the number (-1 for negative, 0 for zero, 1
     *         for positive).
     * @param  magnitude big-endian binary representation of the magnitude of
     *         the number.
     * @throws NumberFormatException {@code signum} is not one of the three
     *         legal values (-1, 0, and 1), or {@code signum} is 0 and
     *         {@code magnitude} contains one or more non-zero bytes.
     */
    public BigInteger(int signum, byte[] magnitude) {
        this.mag = stripLeadingZeroBytes(magnitude);

        if (signum < -1 || signum > 1)
            throw(new NumberFormatException("Invalid signum value"));

        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            if (signum == 0)
                throw(new NumberFormatException("signum-magnitude mismatch"));
            this.signum = signum;
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * A constructor for internal use that translates the sign-magnitude
     * representation of a BigInteger into a BigInteger. It checks the
     * arguments and copies the magnitude so this constructor would be
     * safe for external use.
     */
    private BigInteger(int signum, int[] magnitude) {
        this.mag = stripLeadingZeroInts(magnitude);

        if (signum < -1 || signum > 1)
            throw(new NumberFormatException("Invalid signum value"));

        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            if (signum == 0)
                throw(new NumberFormatException("signum-magnitude mismatch"));
            this.signum = signum;
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * Translates the String representation of a BigInteger in the
     * specified radix into a BigInteger.  The String representation
     * consists of an optional minus or plus sign followed by a
     * sequence of one or more digits in the specified radix.  The
     * character-to-digit mapping is provided by {@code
     * Character.digit}.  The String may not contain any extraneous
     * characters (whitespace, for example).
     *
     * @param val String representation of BigInteger.
     * @param radix radix to be used in interpreting {@code val}.
     * @throws NumberFormatException {@code val} is not a valid representation
     *         of a BigInteger in the specified radix, or {@code radix} is
     *         outside the range from {@link Character#MIN_RADIX} to
     *         {@link Character#MAX_RADIX}, inclusive.
     * @see    Character#digit
     */
    public BigInteger(String val, int radix) {
        int cursor = 0, numDigits;
        final int len = val.length();

        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            throw new NumberFormatException("Radix out of range");
        if (len == 0)
            throw new NumberFormatException("Zero length BigInteger");

        // Check for at most one leading sign
        int sign = 1;
        int index1 = val.lastIndexOf('-');
        int index2 = val.lastIndexOf('+');
        if (index1 >= 0) {
            if (index1 != 0 || index2 >= 0) {
                throw new NumberFormatException("Illegal embedded sign character");
            }
            sign = -1;
            cursor = 1;
        } else if (index2 >= 0) {
            if (index2 != 0) {
                throw new NumberFormatException("Illegal embedded sign character");
            }
            cursor = 1;
        }
        if (cursor == len)
            throw new NumberFormatException("Zero length BigInteger");

        // Skip leading zeros and compute number of digits in magnitude
        while (cursor < len &&
               Character.digit(val.charAt(cursor), radix) == 0) {
            cursor++;
        }

        if (cursor == len) {
            signum = 0;
            mag = ZERO.mag;
            return;
        }

        numDigits = len - cursor;
        signum = sign;

        // Pre-allocate array of expected size. May be too large but can
        // never be too small. Typically exact.
        long numBits = ((numDigits * bitsPerDigit[radix]) >>> 10) + 1;
        if (numBits + 31 >= (1L << 32)) {
            reportOverflow();
        }
        int numWords = (int) (numBits + 31) >>> 5;
        int[] magnitude = new int[numWords];

        // Process first (potentially short) digit group
        int firstGroupLen = numDigits % digitsPerInt[radix];
        if (firstGroupLen == 0)
            firstGroupLen = digitsPerInt[radix];
        String group = val.substring(cursor, cursor += firstGroupLen);
        magnitude[numWords - 1] = Integer.parseInt(group, radix);
        if (magnitude[numWords - 1] < 0)
            throw new NumberFormatException("Illegal digit");

        // Process remaining digit groups
        int superRadix = intRadix[radix];
        int groupVal = 0;
        while (cursor < len) {
            group = val.substring(cursor, cursor += digitsPerInt[radix]);
            groupVal = Integer.parseInt(group, radix);
            if (groupVal < 0)
                throw new NumberFormatException("Illegal digit");
            destructiveMulAdd(magnitude, superRadix, groupVal);
        }
        // Required for cases where the array was overallocated.
        mag = trustedStripLeadingZeroInts(magnitude);
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /*
     * Constructs a new BigInteger using a char array with radix=10.
     * Sign is precalculated outside and not allowed in the val.
     */
    BigInteger(char[] val, int sign, int len) {
        int cursor = 0, numDigits;

        // Skip leading zeros and compute number of digits in magnitude
        while (cursor < len && Character.digit(val[cursor], 10) == 0) {
            cursor++;
        }
        if (cursor == len) {
            signum = 0;
            mag = ZERO.mag;
            return;
        }

        numDigits = len - cursor;
        signum = sign;
        // Pre-allocate array of expected size
        int numWords;
        if (len < 10) {
            numWords = 1;
        } else {
            long numBits = ((numDigits * bitsPerDigit[10]) >>> 10) + 1;
            if (numBits + 31 >= (1L << 32)) {
                reportOverflow();
            }
            numWords = (int) (numBits + 31) >>> 5;
        }
        int[] magnitude = new int[numWords];

        // Process first (potentially short) digit group
        int firstGroupLen = numDigits % digitsPerInt[10];
        if (firstGroupLen == 0)
            firstGroupLen = digitsPerInt[10];
        magnitude[numWords - 1] = parseInt(val, cursor,  cursor += firstGroupLen);

        // Process remaining digit groups
        while (cursor < len) {
            int groupVal = parseInt(val, cursor, cursor += digitsPerInt[10]);
            destructiveMulAdd(magnitude, intRadix[10], groupVal);
        }
        mag = trustedStripLeadingZeroInts(magnitude);
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    // Create an integer with the digits between the two indexes
    // Assumes start < end. The result may be negative, but it
    // is to be treated as an unsigned value.
    private int parseInt(char[] source, int start, int end) {
        int result = Character.digit(source[start++], 10);
        if (result == -1)
            throw new NumberFormatException(new String(source));

        for (int index = start; index < end; index++) {
            int nextVal = Character.digit(source[index], 10);
            if (nextVal == -1)
                throw new NumberFormatException(new String(source));
            result = 10*result + nextVal;
        }

        return result;
    }

    // bitsPerDigit in the given radix times 1024
    // Rounded up to avoid underallocation.
    private static long bitsPerDigit[] = { 0, 0,
        1024, 1624, 2048, 2378, 2648, 2875, 3072, 3247, 3402, 3543, 3672,
        3790, 3899, 4001, 4096, 4186, 4271, 4350, 4426, 4498, 4567, 4633,
        4696, 4756, 4814, 4870, 4923, 4975, 5025, 5074, 5120, 5166, 5210,
                                           5253, 5295};

    // Multiply x array times word y in place, and add word z
    private static void destructiveMulAdd(int[] x, int y, int z) {
        // Perform the multiplication word by word
        long ylong = y & LONG_MASK;
        long zlong = z & LONG_MASK;
        int len = x.length;

        long product = 0;
        long carry = 0;
        for (int i = len-1; i >= 0; i--) {
            product = ylong * (x[i] & LONG_MASK) + carry;
            x[i] = (int)product;
            carry = product >>> 32;
        }

        // Perform the addition
        long sum = (x[len-1] & LONG_MASK) + zlong;
        x[len-1] = (int)sum;
        carry = sum >>> 32;
        for (int i = len-2; i >= 0; i--) {
            sum = (x[i] & LONG_MASK) + carry;
            x[i] = (int)sum;
            carry = sum >>> 32;
        }
    }

    /**
     * Translates the decimal String representation of a BigInteger into a
     * BigInteger.  The String representation consists of an optional minus
     * sign followed by a sequence of one or more decimal digits.  The
     * character-to-digit mapping is provided by {@code Character.digit}.
     * The String may not contain any extraneous characters (whitespace, for
     * example).
     *
     * @param val decimal String representation of BigInteger.
     * @throws NumberFormatException {@code val} is not a valid representation
     *         of a BigInteger.
     * @see    Character#digit
     */
    public BigInteger(String val) {
        this(val, 10);
    }

    /**
     * Constructs a randomly generated BigInteger, uniformly distributed over
     * the range 0 to (2<sup>{@code numBits}</sup> - 1), inclusive.
     * The uniformity of the distribution assumes that a fair source of random
     * bits is provided in {@code rnd}.  Note that this constructor always
     * constructs a non-negative BigInteger.
     *
     * @param  numBits maximum bitLength of the new BigInteger.
     * @param  rnd source of randomness to be used in computing the new
     *         BigInteger.
     * @throws IllegalArgumentException {@code numBits} is negative.
     * @see #bitLength()
     */
    public BigInteger(int numBits, Random rnd) {
        this(1, randomBits(numBits, rnd));
    }

    private static byte[] randomBits(int numBits, Random rnd) {
        if (numBits < 0)
            throw new IllegalArgumentException("numBits must be non-negative");
        int numBytes = (int)(((long)numBits+7)/8); // avoid overflow
        byte[] randomBits = new byte[numBytes];

        // Generate random bytes and mask out any excess bits
        if (numBytes > 0) {
            rnd.nextBytes(randomBits);
            int excessBits = 8*numBytes - numBits;
            randomBits[0] &= (1 << (8-excessBits)) - 1;
        }
        return randomBits;
    }

    /**
     * Constructs a randomly generated positive BigInteger that is probably
     * prime, with the specified bitLength.
     *
     * <p>It is recommended that the {@link #probablePrime probablePrime}
     * method be used in preference to this constructor unless there
     * is a compelling need to specify a certainty.
     *
     * @param  bitLength bitLength of the returned BigInteger.
     * @param  certainty a measure of the uncertainty that the caller is
     *         willing to tolerate.  The probability that the new BigInteger
     *         represents a prime number will exceed
     *         (1 - 1/2<sup>{@code certainty}</sup>).  The execution time of
     *         this constructor is proportional to the value of this parameter.
     * @param  rnd source of random bits used to select candidates to be
     *         tested for primality.
     * @throws ArithmeticException {@code bitLength < 2} or {@code bitLength} is too large.
     * @see    #bitLength()
     */
    public BigInteger(int bitLength, int certainty, Random rnd) {
        BigInteger prime;

        if (bitLength < 2)
            throw new ArithmeticException("bitLength < 2");
        prime = (bitLength < SMALL_PRIME_THRESHOLD
                                ? smallPrime(bitLength, certainty, rnd)
                                : largePrime(bitLength, certainty, rnd));
        signum = 1;
        mag = prime.mag;
    }

    // Minimum size in bits that the requested prime number has
    // before we use the large prime number generating algorithms.
    // The cutoff of 95 was chosen empirically for best performance.
    private static final int SMALL_PRIME_THRESHOLD = 95;

    // Certainty required to meet the spec of probablePrime
    private static final int DEFAULT_PRIME_CERTAINTY = 100;

    /**
     * Returns a positive BigInteger that is probably prime, with the
     * specified bitLength. The probability that a BigInteger returned
     * by this method is composite does not exceed 2<sup>-100</sup>.
     *
     * @param  bitLength bitLength of the returned BigInteger.
     * @param  rnd source of random bits used to select candidates to be
     *         tested for primality.
     * @return a BigInteger of {@code bitLength} bits that is probably prime
     * @throws ArithmeticException {@code bitLength < 2} or {@code bitLength} is too large.
     * @see    #bitLength()
     * @since 1.4
     */
    public static BigInteger probablePrime(int bitLength, Random rnd) {
        if (bitLength < 2)
            throw new ArithmeticException("bitLength < 2");

        return (bitLength < SMALL_PRIME_THRESHOLD ?
                smallPrime(bitLength, DEFAULT_PRIME_CERTAINTY, rnd) :
                largePrime(bitLength, DEFAULT_PRIME_CERTAINTY, rnd));
    }

    /**
     * Find a random number of the specified bitLength that is probably prime.
     * This method is used for smaller primes, its performance degrades on
     * larger bitlengths.
     *
     * This method assumes bitLength > 1.
     */
    private static BigInteger smallPrime(int bitLength, int certainty, Random rnd) {
        int magLen = (bitLength + 31) >>> 5;
        int temp[] = new int[magLen];
        int highBit = 1 << ((bitLength+31) & 0x1f);  // High bit of high int
        int highMask = (highBit << 1) - 1;  // Bits to keep in high int

        while (true) {
            // Construct a candidate
            for (int i=0; i < magLen; i++)
                temp[i] = rnd.nextInt();
            temp[0] = (temp[0] & highMask) | highBit;  // Ensure exact length
            if (bitLength > 2)
                temp[magLen-1] |= 1;  // Make odd if bitlen > 2

            BigInteger p = new BigInteger(temp, 1);

            // Do cheap "pre-test" if applicable
            if (bitLength > 6) {
                long r = p.remainder(SMALL_PRIME_PRODUCT).longValue();
                if ((r%3==0)  || (r%5==0)  || (r%7==0)  || (r%11==0) ||
                    (r%13==0) || (r%17==0) || (r%19==0) || (r%23==0) ||
                    (r%29==0) || (r%31==0) || (r%37==0) || (r%41==0))
                    continue; // Candidate is composite; try another
            }

            // All candidates of bitLength 2 and 3 are prime by this point
            if (bitLength < 4)
                return p;

            // Do expensive test if we survive pre-test (or it's inapplicable)
            if (p.primeToCertainty(certainty, rnd))
                return p;
        }
    }

    private static final BigInteger SMALL_PRIME_PRODUCT
                       = valueOf(3L*5*7*11*13*17*19*23*29*31*37*41);

    /**
     * Find a random number of the specified bitLength that is probably prime.
     * This method is more appropriate for larger bitlengths since it uses
     * a sieve to eliminate most composites before using a more expensive
     * test.
     */
    private static BigInteger largePrime(int bitLength, int certainty, Random rnd) {
        BigInteger p;
        p = new BigInteger(bitLength, rnd).setBit(bitLength-1);
        p.mag[p.mag.length-1] &= 0xfffffffe;

        // Use a sieve length likely to contain the next prime number
        int searchLen = getPrimeSearchLen(bitLength);
        BitSieve searchSieve = new BitSieve(p, searchLen);
        BigInteger candidate = searchSieve.retrieve(p, certainty, rnd);

        while ((candidate == null) || (candidate.bitLength() != bitLength)) {
            p = p.add(BigInteger.valueOf(2*searchLen));
            if (p.bitLength() != bitLength)
                p = new BigInteger(bitLength, rnd).setBit(bitLength-1);
            p.mag[p.mag.length-1] &= 0xfffffffe;
            searchSieve = new BitSieve(p, searchLen);
            candidate = searchSieve.retrieve(p, certainty, rnd);
        }
        return candidate;
    }

   /**
    * Returns the first integer greater than this {@code BigInteger} that
    * is probably prime.  The probability that the number returned by this
    * method is composite does not exceed 2<sup>-100</sup>. This method will
    * never skip over a prime when searching: if it returns {@code p}, there
    * is no prime {@code q} such that {@code this < q < p}.
    *
    * @return the first integer greater than this {@code BigInteger} that
    *         is probably prime.
    * @throws ArithmeticException {@code this < 0} or {@code this} is too large.
    * @since 1.5
    */
    public BigInteger nextProbablePrime() {
        if (this.signum < 0)
            throw new ArithmeticException("start < 0: " + this);

        // Handle trivial cases
        if ((this.signum == 0) || this.equals(ONE))
            return TWO;

        BigInteger result = this.add(ONE);

        // Fastpath for small numbers
        if (result.bitLength() < SMALL_PRIME_THRESHOLD) {

            // Ensure an odd number
            if (!result.testBit(0))
                result = result.add(ONE);

            while (true) {
                // Do cheap "pre-test" if applicable
                if (result.bitLength() > 6) {
                    long r = result.remainder(SMALL_PRIME_PRODUCT).longValue();
                    if ((r%3==0)  || (r%5==0)  || (r%7==0)  || (r%11==0) ||
                        (r%13==0) || (r%17==0) || (r%19==0) || (r%23==0) ||
                        (r%29==0) || (r%31==0) || (r%37==0) || (r%41==0)) {
                        result = result.add(TWO);
                        continue; // Candidate is composite; try another
                    }
                }

                // All candidates of bitLength 2 and 3 are prime by this point
                if (result.bitLength() < 4)
                    return result;

                // The expensive test
                if (result.primeToCertainty(DEFAULT_PRIME_CERTAINTY, null))
                    return result;

                result = result.add(TWO);
            }
        }

        // Start at previous even number
        if (result.testBit(0))
            result = result.subtract(ONE);

        // Looking for the next large prime
        int searchLen = getPrimeSearchLen(result.bitLength());

        while (true) {
           BitSieve searchSieve = new BitSieve(result, searchLen);
           BigInteger candidate = searchSieve.retrieve(result,
                                                 DEFAULT_PRIME_CERTAINTY, null);
           if (candidate != null)
               return candidate;
           result = result.add(BigInteger.valueOf(2 * searchLen));
        }
    }

    private static int getPrimeSearchLen(int bitLength) {
        if (bitLength > PRIME_SEARCH_BIT_LENGTH_LIMIT + 1) {
            throw new ArithmeticException("Prime search implementation restriction on bitLength");
        }
        return bitLength / 20 * 64;
    }

    /**
     * Returns {@code true} if this BigInteger is probably prime,
     * {@code false} if it's definitely composite.
     *
     * This method assumes bitLength > 2.
     *
     * @param  certainty a measure of the uncertainty that the caller is
     *         willing to tolerate: if the call returns {@code true}
     *         the probability that this BigInteger is prime exceeds
     *         {@code (1 - 1/2<sup>certainty</sup>)}.  The execution time of
     *         this method is proportional to the value of this parameter.
     * @return {@code true} if this BigInteger is probably prime,
     *         {@code false} if it's definitely composite.
     */
    boolean primeToCertainty(int certainty, Random random) {
        int rounds = 0;
        int n = (Math.min(certainty, Integer.MAX_VALUE-1)+1)/2;

        // The relationship between the certainty and the number of rounds
        // we perform is given in the draft standard ANSI X9.80, "PRIME
        // NUMBER GENERATION, PRIMALITY TESTING, AND PRIMALITY CERTIFICATES".
        int sizeInBits = this.bitLength();
        if (sizeInBits < 100) {
            rounds = 50;
            rounds = n < rounds ? n : rounds;
            return passesMillerRabin(rounds, random);
        }

        if (sizeInBits < 256) {
            rounds = 27;
        } else if (sizeInBits < 512) {
            rounds = 15;
        } else if (sizeInBits < 768) {
            rounds = 8;
        } else if (sizeInBits < 1024) {
            rounds = 4;
        } else {
            rounds = 2;
        }
        rounds = n < rounds ? n : rounds;

        return passesMillerRabin(rounds, random) && passesLucasLehmer();
    }

    /**
     * Returns true iff this BigInteger is a Lucas-Lehmer probable prime.
     *
     * The following assumptions are made:
     * This BigInteger is a positive, odd number.
     */
    private boolean passesLucasLehmer() {
        BigInteger thisPlusOne = this.add(ONE);

        // Step 1
        int d = 5;
        while (jacobiSymbol(d, this) != -1) {
            // 5, -7, 9, -11, ...
            d = (d < 0) ? Math.abs(d)+2 : -(d+2);
        }

        // Step 2
        BigInteger u = lucasLehmerSequence(d, thisPlusOne, this);

        // Step 3
        return u.mod(this).equals(ZERO);
    }

    /**
     * Computes Jacobi(p,n).
     * Assumes n positive, odd, n>=3.
     */
    private static int jacobiSymbol(int p, BigInteger n) {
        if (p == 0)
            return 0;

        // Algorithm and comments adapted from Colin Plumb's C library.
        int j = 1;
        int u = n.mag[n.mag.length-1];

        // Make p positive
        if (p < 0) {
            p = -p;
            int n8 = u & 7;
            if ((n8 == 3) || (n8 == 7))
                j = -j; // 3 (011) or 7 (111) mod 8
        }

        // Get rid of factors of 2 in p
        while ((p & 3) == 0)
            p >>= 2;
        if ((p & 1) == 0) {
            p >>= 1;
            if (((u ^ (u>>1)) & 2) != 0)
                j = -j; // 3 (011) or 5 (101) mod 8
        }
        if (p == 1)
            return j;
        // Then, apply quadratic reciprocity
        if ((p & u & 2) != 0)   // p = u = 3 (mod 4)?
            j = -j;
        // And reduce u mod p
        u = n.mod(BigInteger.valueOf(p)).intValue();

        // Now compute Jacobi(u,p), u < p
        while (u != 0) {
            while ((u & 3) == 0)
                u >>= 2;
            if ((u & 1) == 0) {
                u >>= 1;
                if (((p ^ (p>>1)) & 2) != 0)
                    j = -j;     // 3 (011) or 5 (101) mod 8
            }
            if (u == 1)
                return j;
            // Now both u and p are odd, so use quadratic reciprocity
            assert (u < p);
            int t = u; u = p; p = t;
            if ((u & p & 2) != 0) // u = p = 3 (mod 4)?
                j = -j;
            // Now u >= p, so it can be reduced
            u %= p;
        }
        return 0;
    }

    private static BigInteger lucasLehmerSequence(int z, BigInteger k, BigInteger n) {
        BigInteger d = BigInteger.valueOf(z);
        BigInteger u = ONE; BigInteger u2;
        BigInteger v = ONE; BigInteger v2;

        for (int i=k.bitLength()-2; i >= 0; i--) {
            u2 = u.multiply(v).mod(n);

            v2 = v.square().add(d.multiply(u.square())).mod(n);
            if (v2.testBit(0))
                v2 = v2.subtract(n);

            v2 = v2.shiftRight(1);

            u = u2; v = v2;
            if (k.testBit(i)) {
                u2 = u.add(v).mod(n);
                if (u2.testBit(0))
                    u2 = u2.subtract(n);

                u2 = u2.shiftRight(1);
                v2 = v.add(d.multiply(u)).mod(n);
                if (v2.testBit(0))
                    v2 = v2.subtract(n);
                v2 = v2.shiftRight(1);

                u = u2; v = v2;
            }
        }
        return u;
    }

    /**
     * Returns true iff this BigInteger passes the specified number of
     * Miller-Rabin tests. This test is taken from the DSA spec (NIST FIPS
     * 186-2).
     *
     * The following assumptions are made:
     * This BigInteger is a positive, odd number greater than 2.
     * iterations<=50.
     */
    private boolean passesMillerRabin(int iterations, Random rnd) {
        // Find a and m such that m is odd and this == 1 + 2**a * m
        BigInteger thisMinusOne = this.subtract(ONE);
        BigInteger m = thisMinusOne;
        int a = m.getLowestSetBit();
        m = m.shiftRight(a);

        // Do the tests
        if (rnd == null) {
            rnd = ThreadLocalRandom.current();
        }
        for (int i=0; i < iterations; i++) {
            // Generate a uniform random on (1, this)
            BigInteger b;
            do {
                b = new BigInteger(this.bitLength(), rnd);
            } while (b.compareTo(ONE) <= 0 || b.compareTo(this) >= 0);

            int j = 0;
            BigInteger z = b.modPow(m, this);
            while (!((j == 0 && z.equals(ONE)) || z.equals(thisMinusOne))) {
                if (j > 0 && z.equals(ONE) || ++j == a)
                    return false;
                z = z.modPow(TWO, this);
            }
        }
        return true;
    }

    /**
     * This internal constructor differs from its public cousin
     * with the arguments reversed in two ways: it assumes that its
     * arguments are correct, and it doesn't copy the magnitude array.
     */
    BigInteger(int[] magnitude, int signum) {
        this.signum = (magnitude.length == 0 ? 0 : signum);
        this.mag = magnitude;
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * This private constructor is for internal use and assumes that its
     * arguments are correct.
     */
    private BigInteger(byte[] magnitude, int signum) {
        this.signum = (magnitude.length == 0 ? 0 : signum);
        this.mag = stripLeadingZeroBytes(magnitude);
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * Throws an {@code ArithmeticException} if the {@code BigInteger} would be
     * out of the supported range.
     *
     * @throws ArithmeticException if {@code this} exceeds the supported range.
     */
    private void checkRange() {
        if (mag.length > MAX_MAG_LENGTH || mag.length == MAX_MAG_LENGTH && mag[0] < 0) {
            reportOverflow();
        }
    }

    private static void reportOverflow() {
        throw new ArithmeticException("BigInteger would overflow supported range");
    }

    //Static Factory Methods

    /**
     * Returns a BigInteger whose value is equal to that of the
     * specified {@code long}.  This "static factory method" is
     * provided in preference to a ({@code long}) constructor
     * because it allows for reuse of frequently used BigIntegers.
     *
     * @param  val value of the BigInteger to return.
     * @return a BigInteger with the specified value.
     */
    public static BigInteger valueOf(long val) {
        // If -MAX_CONSTANT < val < MAX_CONSTANT, return stashed constant
        if (val == 0)
            return ZERO;
        if (val > 0 && val <= MAX_CONSTANT)
            return posConst[(int) val];
        else if (val < 0 && val >= -MAX_CONSTANT)
            return negConst[(int) -val];

        return new BigInteger(val);
    }

    /**
     * Constructs a BigInteger with the specified value, which may not be zero.
     */
    private BigInteger(long val) {
        if (val < 0) {
            val = -val;
            signum = -1;
        } else {
            signum = 1;
        }

        int highWord = (int)(val >>> 32);
        if (highWord == 0) {
            mag = new int[1];
            mag[0] = (int)val;
        } else {
            mag = new int[2];
            mag[0] = highWord;
            mag[1] = (int)val;
        }
    }

    /**
     * Returns a BigInteger with the given two's complement representation.
     * Assumes that the input array will not be modified (the returned
     * BigInteger will reference the input array if feasible).
     */
    private static BigInteger valueOf(int val[]) {
        return (val[0] > 0 ? new BigInteger(val, 1) : new BigInteger(val));
    }

    // Constants

    /**
     * Initialize static constant array when class is loaded.
     */
    private final static int MAX_CONSTANT = 16;
    private static BigInteger posConst[] = new BigInteger[MAX_CONSTANT+1];
    private static BigInteger negConst[] = new BigInteger[MAX_CONSTANT+1];

    /**
     * The cache of powers of each radix.  This allows us to not have to
     * recalculate powers of radix^(2^n) more than once.  This speeds
     * Schoenhage recursive base conversion significantly.
     */
    private static volatile BigInteger[][] powerCache;

    /** The cache of logarithms of radices for base conversion. */
    private static final double[] logCache;

    /** The natural log of 2.  This is used in computing cache indices. */
    private static final double LOG_TWO = Math.log(2.0);

    static {
        assert 0 < KARATSUBA_THRESHOLD
            && KARATSUBA_THRESHOLD < TOOM_COOK_THRESHOLD
            && TOOM_COOK_THRESHOLD < Integer.MAX_VALUE
            && 0 < KARATSUBA_SQUARE_THRESHOLD
            && KARATSUBA_SQUARE_THRESHOLD < TOOM_COOK_SQUARE_THRESHOLD
            && TOOM_COOK_SQUARE_THRESHOLD < Integer.MAX_VALUE :
            "Algorithm thresholds are inconsistent";

        for (int i = 1; i <= MAX_CONSTANT; i++) {
            int[] magnitude = new int[1];
            magnitude[0] = i;
            posConst[i] = new BigInteger(magnitude,  1);
            negConst[i] = new BigInteger(magnitude, -1);
        }

        /*
         * Initialize the cache of radix^(2^x) values used for base conversion
         * with just the very first value.  Additional values will be created
         * on demand.
         */
        powerCache = new BigInteger[Character.MAX_RADIX+1][];
        logCache = new double[Character.MAX_RADIX+1];

        for (int i=Character.MIN_RADIX; i <= Character.MAX_RADIX; i++) {
            powerCache[i] = new BigInteger[] { BigInteger.valueOf(i) };
            logCache[i] = Math.log(i);
        }
    }

    /**
     * The BigInteger constant zero.
     *
     * @since   1.2
     */
    public static final BigInteger ZERO = new BigInteger(new int[0], 0);

    /**
     * The BigInteger constant one.
     *
     * @since   1.2
     */
    public static final BigInteger ONE = valueOf(1);

    /**
     * The BigInteger constant two.  (Not exported.)
     */
    private static final BigInteger TWO = valueOf(2);

    /**
     * The BigInteger constant -1.  (Not exported.)
     */
    private static final BigInteger NEGATIVE_ONE = valueOf(-1);

    /**
     * The BigInteger constant ten.
     *
     * @since   1.5
     */
    public static final BigInteger TEN = valueOf(10);

    // Arithmetic Operations

    /**
     * Returns a BigInteger whose value is {@code (this + val)}.
     *
     * @param  val value to be added to this BigInteger.
     * @return {@code this + val}
     */
    public BigInteger add(BigInteger val) {
        if (val.signum == 0)
            return this;
        if (signum == 0)
            return val;
        if (val.signum == signum)
            return new BigInteger(add(mag, val.mag), signum);

        int cmp = compareMagnitude(val);
        if (cmp == 0)
            return ZERO;
        int[] resultMag = (cmp > 0 ? subtract(mag, val.mag)
                           : subtract(val.mag, mag));
        resultMag = trustedStripLeadingZeroInts(resultMag);

        return new BigInteger(resultMag, cmp == signum ? 1 : -1);
    }

    /**
     * Package private methods used by BigDecimal code to add a BigInteger
     * with a long. Assumes val is not equal to INFLATED.
     */
    BigInteger add(long val) {
        if (val == 0)
            return this;
        if (signum == 0)
            return valueOf(val);
        if (Long.signum(val) == signum)
            return new BigInteger(add(mag, Math.abs(val)), signum);
        int cmp = compareMagnitude(val);
        if (cmp == 0)
            return ZERO;
        int[] resultMag = (cmp > 0 ? subtract(mag, Math.abs(val)) : subtract(Math.abs(val), mag));
        resultMag = trustedStripLeadingZeroInts(resultMag);
        return new BigInteger(resultMag, cmp == signum ? 1 : -1);
    }

    /**
     * Adds the contents of the int array x and long value val. This
     * method allocates a new int array to hold the answer and returns
     * a reference to that array.  Assumes x.length &gt; 0 and val is
     * non-negative
     */
    private static int[] add(int[] x, long val) {
        int[] y;
        long sum = 0;
        int xIndex = x.length;
        int[] result;
        int highWord = (int)(val >>> 32);
        if (highWord == 0) {
            result = new int[xIndex];
            sum = (x[--xIndex] & LONG_MASK) + val;
            result[xIndex] = (int)sum;
        } else {
            if (xIndex == 1) {
                result = new int[2];
                sum = val  + (x[0] & LONG_MASK);
                result[1] = (int)sum;
                result[0] = (int)(sum >>> 32);
                return result;
            } else {
                result = new int[xIndex];
                sum = (x[--xIndex] & LONG_MASK) + (val & LONG_MASK);
                result[xIndex] = (int)sum;
                sum = (x[--xIndex] & LONG_MASK) + (highWord & LONG_MASK) + (sum >>> 32);
                result[xIndex] = (int)sum;
            }
        }
        // Copy remainder of longer number while carry propagation is required
        boolean carry = (sum >>> 32 != 0);
        while (xIndex > 0 && carry)
            carry = ((result[--xIndex] = x[xIndex] + 1) == 0);
        // Copy remainder of longer number
        while (xIndex > 0)
            result[--xIndex] = x[xIndex];
        // Grow result if necessary
        if (carry) {
            int bigger[] = new int[result.length + 1];
            System.arraycopy(result, 0, bigger, 1, result.length);
            bigger[0] = 0x01;
            return bigger;
        }
        return result;
    }

    /**
     * Adds the contents of the int arrays x and y. This method allocates
     * a new int array to hold the answer and returns a reference to that
     * array.
     */
    private static int[] add(int[] x, int[] y) {
        // If x is shorter, swap the two arrays
        if (x.length < y.length) {
            int[] tmp = x;
            x = y;
            y = tmp;
        }

        int xIndex = x.length;
        int yIndex = y.length;
        int result[] = new int[xIndex];
        long sum = 0;
        if (yIndex == 1) {
            sum = (x[--xIndex] & LONG_MASK) + (y[0] & LONG_MASK) ;
            result[xIndex] = (int)sum;
        } else {
            // Add common parts of both numbers
            while (yIndex > 0) {
                sum = (x[--xIndex] & LONG_MASK) +
                      (y[--yIndex] & LONG_MASK) + (sum >>> 32);
                result[xIndex] = (int)sum;
            }
        }
        // Copy remainder of longer number while carry propagation is required
        boolean carry = (sum >>> 32 != 0);
        while (xIndex > 0 && carry)
            carry = ((result[--xIndex] = x[xIndex] + 1) == 0);

        // Copy remainder of longer number
        while (xIndex > 0)
            result[--xIndex] = x[xIndex];

        // Grow result if necessary
        if (carry) {
            int bigger[] = new int[result.length + 1];
            System.arraycopy(result, 0, bigger, 1, result.length);
            bigger[0] = 0x01;
            return bigger;
        }
        return result;
    }

    private static int[] subtract(long val, int[] little) {
        int highWord = (int)(val >>> 32);
        if (highWord == 0) {
            int result[] = new int[1];
            result[0] = (int)(val - (little[0] & LONG_MASK));
            return result;
        } else {
            int result[] = new int[2];
            if (little.length == 1) {
                long difference = ((int)val & LONG_MASK) - (little[0] & LONG_MASK);
                result[1] = (int)difference;
                // Subtract remainder of longer number while borrow propagates
                boolean borrow = (difference >> 32 != 0);
                if (borrow) {
                    result[0] = highWord - 1;
                } else {        // Copy remainder of longer number
                    result[0] = highWord;
                }
                return result;
            } else { // little.length == 2
                long difference = ((int)val & LONG_MASK) - (little[1] & LONG_MASK);
                result[1] = (int)difference;
                difference = (highWord & LONG_MASK) - (little[0] & LONG_MASK) + (difference >> 32);
                result[0] = (int)difference;
                return result;
            }
        }
    }

    /**
     * Subtracts the contents of the second argument (val) from the
     * first (big).  The first int array (big) must represent a larger number
     * than the second.  This method allocates the space necessary to hold the
     * answer.
     * assumes val &gt;= 0
     */
    private static int[] subtract(int[] big, long val) {
        int highWord = (int)(val >>> 32);
        int bigIndex = big.length;
        int result[] = new int[bigIndex];
        long difference = 0;

        if (highWord == 0) {
            difference = (big[--bigIndex] & LONG_MASK) - val;
            result[bigIndex] = (int)difference;
        } else {
            difference = (big[--bigIndex] & LONG_MASK) - (val & LONG_MASK);
            result[bigIndex] = (int)difference;
            difference = (big[--bigIndex] & LONG_MASK) - (highWord & LONG_MASK) + (difference >> 32);
            result[bigIndex] = (int)difference;
        }

        // Subtract remainder of longer number while borrow propagates
        boolean borrow = (difference >> 32 != 0);
        while (bigIndex > 0 && borrow)
            borrow = ((result[--bigIndex] = big[bigIndex] - 1) == -1);

        // Copy remainder of longer number
        while (bigIndex > 0)
            result[--bigIndex] = big[bigIndex];

        return result;
    }

    /**
     * Returns a BigInteger whose value is {@code (this - val)}.
     *
     * @param  val value to be subtracted from this BigInteger.
     * @return {@code this - val}
     */
    public BigInteger subtract(BigInteger val) {
        if (val.signum == 0)
            return this;
        if (signum == 0)
            return val.negate();
        if (val.signum != signum)
            return new BigInteger(add(mag, val.mag), signum);

        int cmp = compareMagnitude(val);
        if (cmp == 0)
            return ZERO;
        int[] resultMag = (cmp > 0 ? subtract(mag, val.mag)
                           : subtract(val.mag, mag));
        resultMag = trustedStripLeadingZeroInts(resultMag);
        return new BigInteger(resultMag, cmp == signum ? 1 : -1);
    }

    /**
     * Subtracts the contents of the second int arrays (little) from the
     * first (big).  The first int array (big) must represent a larger number
     * than the second.  This method allocates the space necessary to hold the
     * answer.
     */
    private static int[] subtract(int[] big, int[] little) {
        int bigIndex = big.length;
        int result[] = new int[bigIndex];
        int littleIndex = little.length;
        long difference = 0;

        // Subtract common parts of both numbers
        while (littleIndex > 0) {
            difference = (big[--bigIndex] & LONG_MASK) -
                         (little[--littleIndex] & LONG_MASK) +
                         (difference >> 32);
            result[bigIndex] = (int)difference;
        }

        // Subtract remainder of longer number while borrow propagates
        boolean borrow = (difference >> 32 != 0);
        while (bigIndex > 0 && borrow)
            borrow = ((result[--bigIndex] = big[bigIndex] - 1) == -1);

        // Copy remainder of longer number
        while (bigIndex > 0)
            result[--bigIndex] = big[bigIndex];

        return result;
    }

    /**
     * Returns a BigInteger whose value is {@code (this * val)}.
     *
     * @implNote An implementation may offer better algorithmic
     * performance when {@code val == this}.
     *
     * @param  val value to be multiplied by this BigInteger.
     * @return {@code this * val}
     */
    public BigInteger multiply(BigInteger val) {
        return multiply(val, false);
    }

    /**
     * Returns a BigInteger whose value is {@code (this * val)}.  If
     * the invocation is recursive certain overflow checks are skipped.
     *
     * @param  val value to be multiplied by this BigInteger.
     * @param  isRecursion whether this is a recursive invocation
     * @return {@code this * val}
     */
    private BigInteger multiply(BigInteger val, boolean isRecursion) {
        if (val.signum == 0 || signum == 0)
            return ZERO;

        int xlen = mag.length;

        if (val == this && xlen > MULTIPLY_SQUARE_THRESHOLD) {
            return square();
        }

        int ylen = val.mag.length;

        if ((xlen < KARATSUBA_THRESHOLD) || (ylen < KARATSUBA_THRESHOLD)) {
            int resultSign = signum == val.signum ? 1 : -1;
            if (val.mag.length == 1) {
                return multiplyByInt(mag,val.mag[0], resultSign);
            }
            if (mag.length == 1) {
                return multiplyByInt(val.mag,mag[0], resultSign);
            }
            int[] result = multiplyToLen(mag, xlen,
                                         val.mag, ylen, null);
            result = trustedStripLeadingZeroInts(result);
            return new BigInteger(result, resultSign);
        } else {
            if ((xlen < TOOM_COOK_THRESHOLD) && (ylen < TOOM_COOK_THRESHOLD)) {
                return multiplyKaratsuba(this, val);
            } else {
                //
                // In "Hacker's Delight" section 2-13, p.33, it is explained
                // that if x and y are unsigned 32-bit quantities and m and n
                // are their respective numbers of leading zeros within 32 bits,
                // then the number of leading zeros within their product as a
                // 64-bit unsigned quantity is either m + n or m + n + 1. If
                // their product is not to overflow, it cannot exceed 32 bits,
                // and so the number of leading zeros of the product within 64
                // bits must be at least 32, i.e., the leftmost set bit is at
                // zero-relative position 31 or less.
                //
                // From the above there are three cases:
                //
                //     m + n    leftmost set bit    condition
                //     -----    ----------------    ---------
                //     >= 32    x <= 64 - 32 = 32   no overflow
                //     == 31    x >= 64 - 32 = 32   possible overflow
                //     <= 30    x >= 64 - 31 = 33   definite overflow
                //
                // The "possible overflow" condition cannot be detected by
                // examning data lengths alone and requires further calculation.
                //
                // By analogy, if 'this' and 'val' have m and n as their
                // respective numbers of leading zeros within 32*MAX_MAG_LENGTH
                // bits, then:
                //
                //     m + n >= 32*MAX_MAG_LENGTH        no overflow
                //     m + n == 32*MAX_MAG_LENGTH - 1    possible overflow
                //     m + n <= 32*MAX_MAG_LENGTH - 2    definite overflow
                //
                // Note however that if the number of ints in the result
                // were to be MAX_MAG_LENGTH and mag[0] < 0, then there would
                // be overflow. As a result the leftmost bit (of mag[0]) cannot
                // be used and the constraints must be adjusted by one bit to:
                //
                //     m + n >  32*MAX_MAG_LENGTH        no overflow
                //     m + n == 32*MAX_MAG_LENGTH        possible overflow
                //     m + n <  32*MAX_MAG_LENGTH        definite overflow
                //
                // The foregoing leading zero-based discussion is for clarity
                // only. The actual calculations use the estimated bit length
                // of the product as this is more natural to the internal
                // array representation of the magnitude which has no leading
                // zero elements.
                //
                if (!isRecursion) {
                    // The bitLength() instance method is not used here as we
                    // are only considering the magnitudes as non-negative. The
                    // Toom-Cook multiplication algorithm determines the sign
                    // at its end from the two signum values.
                    if (bitLength(mag, mag.length) +
                        bitLength(val.mag, val.mag.length) >
                        32L*MAX_MAG_LENGTH) {
                        reportOverflow();
                    }
                }

                return multiplyToomCook3(this, val);
            }
        }
    }

    private static BigInteger multiplyByInt(int[] x, int y, int sign) {
        if (Integer.bitCount(y) == 1) {
            return new BigInteger(shiftLeft(x,Integer.numberOfTrailingZeros(y)), sign);
        }
        int xlen = x.length;
        int[] rmag =  new int[xlen + 1];
        long carry = 0;
        long yl = y & LONG_MASK;
        int rstart = rmag.length - 1;
        for (int i = xlen - 1; i >= 0; i--) {
            long product = (x[i] & LONG_MASK) * yl + carry;
            rmag[rstart--] = (int)product;
            carry = product >>> 32;
        }
        if (carry == 0L) {
            rmag = java.util.Arrays.copyOfRange(rmag, 1, rmag.length);
        } else {
            rmag[rstart] = (int)carry;
        }
        return new BigInteger(rmag, sign);
    }

    /**
     * Package private methods used by BigDecimal code to multiply a BigInteger
     * with a long. Assumes v is not equal to INFLATED.
     */
    BigInteger multiply(long v) {
        if (v == 0 || signum == 0)
          return ZERO;
        if (v == BigDecimal.INFLATED)
            return multiply(BigInteger.valueOf(v));
        int rsign = (v > 0 ? signum : -signum);
        if (v < 0)
            v = -v;
        long dh = v >>> 32;      // higher order bits
        long dl = v & LONG_MASK; // lower order bits

        int xlen = mag.length;
        int[] value = mag;
        int[] rmag = (dh == 0L) ? (new int[xlen + 1]) : (new int[xlen + 2]);
        long carry = 0;
        int rstart = rmag.length - 1;
        for (int i = xlen - 1; i >= 0; i--) {
            long product = (value[i] & LONG_MASK) * dl + carry;
            rmag[rstart--] = (int)product;
            carry = product >>> 32;
        }
        rmag[rstart] = (int)carry;
        if (dh != 0L) {
            carry = 0;
            rstart = rmag.length - 2;
            for (int i = xlen - 1; i >= 0; i--) {
                long product = (value[i] & LONG_MASK) * dh +
                    (rmag[rstart] & LONG_MASK) + carry;
                rmag[rstart--] = (int)product;
                carry = product >>> 32;
            }
            rmag[0] = (int)carry;
        }
        if (carry == 0L)
            rmag = java.util.Arrays.copyOfRange(rmag, 1, rmag.length);
        return new BigInteger(rmag, rsign);
    }

    /**
     * Multiplies int arrays x and y to the specified lengths and places
     * the result into z. There will be no leading zeros in the resultant array.
     */
    private static int[] multiplyToLen(int[] x, int xlen, int[] y, int ylen, int[] z) {
        int xstart = xlen - 1;
        int ystart = ylen - 1;

        if (z == null || z.length < (xlen+ ylen))
             z = new int[xlen+ylen];

        long carry = 0;
        for (int j=ystart, k=ystart+1+xstart; j >= 0; j--, k--) {
            long product = (y[j] & LONG_MASK) *
                           (x[xstart] & LONG_MASK) + carry;
            z[k] = (int)product;
            carry = product >>> 32;
        }
        z[xstart] = (int)carry;

        for (int i = xstart-1; i >= 0; i--) {
            carry = 0;
            for (int j=ystart, k=ystart+1+i; j >= 0; j--, k--) {
                long product = (y[j] & LONG_MASK) *
                               (x[i] & LONG_MASK) +
                               (z[k] & LONG_MASK) + carry;
                z[k] = (int)product;
                carry = product >>> 32;
            }
            z[i] = (int)carry;
        }
        return z;
    }

    /**
     * Multiplies two BigIntegers using the Karatsuba multiplication
     * algorithm.  This is a recursive divide-and-conquer algorithm which is
     * more efficient for large numbers than what is commonly called the
     * "grade-school" algorithm used in multiplyToLen.  If the numbers to be
     * multiplied have length n, the "grade-school" algorithm has an
     * asymptotic complexity of O(n^2).  In contrast, the Karatsuba algorithm
     * has complexity of O(n^(log2(3))), or O(n^1.585).  It achieves this
     * increased performance by doing 3 multiplies instead of 4 when
     * evaluating the product.  As it has some overhead, should be used when
     * both numbers are larger than a certain threshold (found
     * experimentally).
     *
     * See:  http://en.wikipedia.org/wiki/Karatsuba_algorithm
     */
    private static BigInteger multiplyKaratsuba(BigInteger x, BigInteger y) {
        int xlen = x.mag.length;
        int ylen = y.mag.length;

        // The number of ints in each half of the number.
        int half = (Math.max(xlen, ylen)+1) / 2;

        // xl and yl are the lower halves of x and y respectively,
        // xh and yh are the upper halves.
        BigInteger xl = x.getLower(half);
        BigInteger xh = x.getUpper(half);
        BigInteger yl = y.getLower(half);
        BigInteger yh = y.getUpper(half);

        BigInteger p1 = xh.multiply(yh);  // p1 = xh*yh
        BigInteger p2 = xl.multiply(yl);  // p2 = xl*yl

        // p3=(xh+xl)*(yh+yl)
        BigInteger p3 = xh.add(xl).multiply(yh.add(yl));

        // result = p1 * 2^(32*2*half) + (p3 - p1 - p2) * 2^(32*half) + p2
        BigInteger result = p1.shiftLeft(32*half).add(p3.subtract(p1).subtract(p2)).shiftLeft(32*half).add(p2);

        if (x.signum != y.signum) {
            return result.negate();
        } else {
            return result;
        }
    }

    /**
     * Multiplies two BigIntegers using a 3-way Toom-Cook multiplication
     * algorithm.  This is a recursive divide-and-conquer algorithm which is
     * more efficient for large numbers than what is commonly called the
     * "grade-school" algorithm used in multiplyToLen.  If the numbers to be
     * multiplied have length n, the "grade-school" algorithm has an
     * asymptotic complexity of O(n^2).  In contrast, 3-way Toom-Cook has a
     * complexity of about O(n^1.465).  It achieves this increased asymptotic
     * performance by breaking each number into three parts and by doing 5
     * multiplies instead of 9 when evaluating the product.  Due to overhead
     * (additions, shifts, and one division) in the Toom-Cook algorithm, it
     * should only be used when both numbers are larger than a certain
     * threshold (found experimentally).  This threshold is generally larger
     * than that for Karatsuba multiplication, so this algorithm is generally
     * only used when numbers become significantly larger.
     *
     * The algorithm used is the "optimal" 3-way Toom-Cook algorithm outlined
     * by Marco Bodrato.
     *
     *  See: http://bodrato.it/toom-cook/
     *       http://bodrato.it/papers/#WAIFI2007
     *
     * "Towards Optimal Toom-Cook Multiplication for Univariate and
     * Multivariate Polynomials in Characteristic 2 and 0." by Marco BODRATO;
     * In C.Carlet and B.Sunar, Eds., "WAIFI'07 proceedings", p. 116-133,
     * LNCS #4547. Springer, Madrid, Spain, June 21-22, 2007.
     *
     */
    private static BigInteger multiplyToomCook3(BigInteger a, BigInteger b) {
        int alen = a.mag.length;
        int blen = b.mag.length;

        int largest = Math.max(alen, blen);

        // k is the size (in ints) of the lower-order slices.
        int k = (largest+2)/3;   // Equal to ceil(largest/3)

        // r is the size (in ints) of the highest-order slice.
        int r = largest - 2*k;

        // Obtain slices of the numbers. a2 and b2 are the most significant
        // bits of the numbers a and b, and a0 and b0 the least significant.
        BigInteger a0, a1, a2, b0, b1, b2;
        a2 = a.getToomSlice(k, r, 0, largest);
        a1 = a.getToomSlice(k, r, 1, largest);
        a0 = a.getToomSlice(k, r, 2, largest);
        b2 = b.getToomSlice(k, r, 0, largest);
        b1 = b.getToomSlice(k, r, 1, largest);
        b0 = b.getToomSlice(k, r, 2, largest);

        BigInteger v0, v1, v2, vm1, vinf, t1, t2, tm1, da1, db1;

        v0 = a0.multiply(b0, true);
        da1 = a2.add(a0);
        db1 = b2.add(b0);
        vm1 = da1.subtract(a1).multiply(db1.subtract(b1), true);
        da1 = da1.add(a1);
        db1 = db1.add(b1);
        v1 = da1.multiply(db1, true);
        v2 = da1.add(a2).shiftLeft(1).subtract(a0).multiply(
             db1.add(b2).shiftLeft(1).subtract(b0), true);
        vinf = a2.multiply(b2, true);

        // The algorithm requires two divisions by 2 and one by 3.
        // All divisions are known to be exact, that is, they do not produce
        // remainders, and all results are positive.  The divisions by 2 are
        // implemented as right shifts which are relatively efficient, leaving
        // only an exact division by 3, which is done by a specialized
        // linear-time algorithm.
        t2 = v2.subtract(vm1).exactDivideBy3();
        tm1 = v1.subtract(vm1).shiftRight(1);
        t1 = v1.subtract(v0);
        t2 = t2.subtract(t1).shiftRight(1);
        t1 = t1.subtract(tm1).subtract(vinf);
        t2 = t2.subtract(vinf.shiftLeft(1));
        tm1 = tm1.subtract(t2);

        // Number of bits to shift left.
        int ss = k*32;

        BigInteger result = vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1).shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0);

        if (a.signum != b.signum) {
            return result.negate();
        } else {
            return result;
        }
    }


    /**
     * Returns a slice of a BigInteger for use in Toom-Cook multiplication.
     *
     * @param lowerSize The size of the lower-order bit slices.
     * @param upperSize The size of the higher-order bit slices.
     * @param slice The index of which slice is requested, which must be a
     * number from 0 to size-1. Slice 0 is the highest-order bits, and slice
     * size-1 are the lowest-order bits. Slice 0 may be of different size than
     * the other slices.
     * @param fullsize The size of the larger integer array, used to align
     * slices to the appropriate position when multiplying different-sized
     * numbers.
     */
    private BigInteger getToomSlice(int lowerSize, int upperSize, int slice,
                                    int fullsize) {
        int start, end, sliceSize, len, offset;

        len = mag.length;
        offset = fullsize - len;

        if (slice == 0) {
            start = 0 - offset;
            end = upperSize - 1 - offset;
        } else {
            start = upperSize + (slice-1)*lowerSize - offset;
            end = start + lowerSize - 1;
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
           return ZERO;
        }

        sliceSize = (end-start) + 1;

        if (sliceSize <= 0) {
            return ZERO;
        }

        // While performing Toom-Cook, all slices are positive and
        // the sign is adjusted when the final number is composed.
        if (start == 0 && sliceSize >= len) {
            return this.abs();
        }

        int intSlice[] = new int[sliceSize];
        System.arraycopy(mag, start, intSlice, 0, sliceSize);

        return new BigInteger(trustedStripLeadingZeroInts(intSlice), 1);
    }

    /**
     * Does an exact division (that is, the remainder is known to be zero)
     * of the specified number by 3.  This is used in Toom-Cook
     * multiplication.  This is an efficient algorithm that runs in linear
     * time.  If the argument is not exactly divisible by 3, results are
     * undefined.  Note that this is expected to be called with positive
     * arguments only.
     */
    private BigInteger exactDivideBy3() {
        int len = mag.length;
        int[] result = new int[len];
        long x, w, q, borrow;
        borrow = 0L;
        for (int i=len-1; i >= 0; i--) {
            x = (mag[i] & LONG_MASK);
            w = x - borrow;
            if (borrow > x) {      // Did we make the number go negative?
                borrow = 1L;
            } else {
                borrow = 0L;
            }

            // 0xAAAAAAAB is the modular inverse of 3 (mod 2^32).  Thus,
            // the effect of this is to divide by 3 (mod 2^32).
            // This is much faster than division on most architectures.
            q = (w * 0xAAAAAAABL) & LONG_MASK;
            result[i] = (int) q;

            // Now check the borrow. The second check can of course be
            // eliminated if the first fails.
            if (q >= 0x55555556L) {
                borrow++;
                if (q >= 0xAAAAAAABL)
                    borrow++;
            }
        }
        result = trustedStripLeadingZeroInts(result);
        return new BigInteger(result, signum);
    }

    /**
     * Returns a new BigInteger representing n lower ints of the number.
     * This is used by Karatsuba multiplication and Karatsuba squaring.
     */
    private BigInteger getLower(int n) {
        int len = mag.length;

        if (len <= n) {
            return abs();
        }

        int lowerInts[] = new int[n];
        System.arraycopy(mag, len-n, lowerInts, 0, n);

        return new BigInteger(trustedStripLeadingZeroInts(lowerInts), 1);
    }

    /**
     * Returns a new BigInteger representing mag.length-n upper
     * ints of the number.  This is used by Karatsuba multiplication and
     * Karatsuba squaring.
     */
    private BigInteger getUpper(int n) {
        int len = mag.length;

        if (len <= n) {
            return ZERO;
        }

        int upperLen = len - n;
        int upperInts[] = new int[upperLen];
        System.arraycopy(mag, 0, upperInts, 0, upperLen);

        return new BigInteger(trustedStripLeadingZeroInts(upperInts), 1);
    }

    // Squaring

    /**
     * Returns a BigInteger whose value is {@code (this<sup>2</sup>)}.
     *
     * @return {@code this<sup>2</sup>}
     */
    private BigInteger square() {
        return square(false);
    }

    /**
     * Returns a BigInteger whose value is {@code (this<sup>2</sup>)}. If
     * the invocation is recursive certain overflow checks are skipped.
     *
     * @param isRecursion whether this is a recursive invocation
     * @return {@code this<sup>2</sup>}
     */
    private BigInteger square(boolean isRecursion) {
        if (signum == 0) {
            return ZERO;
        }
        int len = mag.length;

        if (len < KARATSUBA_SQUARE_THRESHOLD) {
            int[] z = squareToLen(mag, len, null);
            return new BigInteger(trustedStripLeadingZeroInts(z), 1);
        } else {
            if (len < TOOM_COOK_SQUARE_THRESHOLD) {
                return squareKaratsuba();
            } else {
                //
                // For a discussion of overflow detection see multiply()
                //
                if (!isRecursion) {
                    if (bitLength(mag, mag.length) > 16L*MAX_MAG_LENGTH) {
                        reportOverflow();
                    }
                }

                return squareToomCook3();
            }
        }
    }

    /**
     * Squares the contents of the int array x. The result is placed into the
     * int array z.  The contents of x are not changed.
     */
    private static final int[] squareToLen(int[] x, int len, int[] z) {
         int zlen = len << 1;
         if (z == null || z.length < zlen)
             z = new int[zlen];

         // Execute checks before calling intrinsified method.
         implSquareToLenChecks(x, len, z, zlen);
         return implSquareToLen(x, len, z, zlen);
     }

     /**
      * Parameters validation.
      */
     private static void implSquareToLenChecks(int[] x, int len, int[] z, int zlen) throws RuntimeException {
         if (len < 1) {
             throw new IllegalArgumentException("invalid input length: " + len);
         }
         if (len > x.length) {
             throw new IllegalArgumentException("input length out of bound: " +
                                        len + " > " + x.length);
         }
         if (len * 2 > z.length) {
             throw new IllegalArgumentException("input length out of bound: " +
                                        (len * 2) + " > " + z.length);
         }
         if (zlen < 1) {
             throw new IllegalArgumentException("invalid input length: " + zlen);
         }
         if (zlen > z.length) {
             throw new IllegalArgumentException("input length out of bound: " +
                                        len + " > " + z.length);
         }
     }

     /**
      * Java Runtime may use intrinsic for this method.
      */
     private static final int[] implSquareToLen(int[] x, int len, int[] z, int zlen) {
        /*
         * The algorithm used here is adapted from Colin Plumb's C library.
         * Technique: Consider the partial products in the multiplication
         * of "abcde" by itself:
         *
         *               a  b  c  d  e
         *            *  a  b  c  d  e
         *          ==================
         *              ae be ce de ee
         *           ad bd cd dd de
         *        ac bc cc cd ce
         *     ab bb bc bd be
         *  aa ab ac ad ae
         *
         * Note that everything above the main diagonal:
         *              ae be ce de = (abcd) * e
         *           ad bd cd       = (abc) * d
         *        ac bc             = (ab) * c
         *     ab                   = (a) * b
         *
         * is a copy of everything below the main diagonal:
         *                       de
         *                 cd ce
         *           bc bd be
         *     ab ac ad ae
         *
         * Thus, the sum is 2 * (off the diagonal) + diagonal.
         *
         * This is accumulated beginning with the diagonal (which
         * consist of the squares of the digits of the input), which is then
         * divided by two, the off-diagonal added, and multiplied by two
         * again.  The low bit is simply a copy of the low bit of the
         * input, so it doesn't need special care.
         */

        // Store the squares, right shifted one bit (i.e., divided by 2)
        int lastProductLowWord = 0;
        for (int j=0, i=0; j < len; j++) {
            long piece = (x[j] & LONG_MASK);
            long product = piece * piece;
            z[i++] = (lastProductLowWord << 31) | (int)(product >>> 33);
            z[i++] = (int)(product >>> 1);
            lastProductLowWord = (int)product;
        }

        // Add in off-diagonal sums
        for (int i=len, offset=1; i > 0; i--, offset+=2) {
            int t = x[i-1];
            t = mulAdd(z, x, offset, i-1, t);
            addOne(z, offset-1, i, t);
        }

        // Shift back up and set low bit
        primitiveLeftShift(z, zlen, 1);
        z[zlen-1] |= x[len-1] & 1;

        return z;
    }

    /**
     * Squares a BigInteger using the Karatsuba squaring algorithm.  It should
     * be used when both numbers are larger than a certain threshold (found
     * experimentally).  It is a recursive divide-and-conquer algorithm that
     * has better asymptotic performance than the algorithm used in
     * squareToLen.
     */
    private BigInteger squareKaratsuba() {
        int half = (mag.length+1) / 2;

        BigInteger xl = getLower(half);
        BigInteger xh = getUpper(half);

        BigInteger xhs = xh.square();  // xhs = xh^2
        BigInteger xls = xl.square();  // xls = xl^2

        // xh^2 << 64  +  (((xl+xh)^2 - (xh^2 + xl^2)) << 32) + xl^2
        return xhs.shiftLeft(half*32).add(xl.add(xh).square().subtract(xhs.add(xls))).shiftLeft(half*32).add(xls);
    }

    /**
     * Squares a BigInteger using the 3-way Toom-Cook squaring algorithm.  It
     * should be used when both numbers are larger than a certain threshold
     * (found experimentally).  It is a recursive divide-and-conquer algorithm
     * that has better asymptotic performance than the algorithm used in
     * squareToLen or squareKaratsuba.
     */
    private BigInteger squareToomCook3() {
        int len = mag.length;

        // k is the size (in ints) of the lower-order slices.
        int k = (len+2)/3;   // Equal to ceil(largest/3)

        // r is the size (in ints) of the highest-order slice.
        int r = len - 2*k;

        // Obtain slices of the numbers. a2 is the most significant
        // bits of the number, and a0 the least significant.
        BigInteger a0, a1, a2;
        a2 = getToomSlice(k, r, 0, len);
        a1 = getToomSlice(k, r, 1, len);
        a0 = getToomSlice(k, r, 2, len);
        BigInteger v0, v1, v2, vm1, vinf, t1, t2, tm1, da1;

        v0 = a0.square(true);
        da1 = a2.add(a0);
        vm1 = da1.subtract(a1).square(true);
        da1 = da1.add(a1);
        v1 = da1.square(true);
        vinf = a2.square(true);
        v2 = da1.add(a2).shiftLeft(1).subtract(a0).square(true);

        // The algorithm requires two divisions by 2 and one by 3.
        // All divisions are known to be exact, that is, they do not produce
        // remainders, and all results are positive.  The divisions by 2 are
        // implemented as right shifts which are relatively efficient, leaving
        // only a division by 3.
        // The division by 3 is done by an optimized algorithm for this case.
        t2 = v2.subtract(vm1).exactDivideBy3();
        tm1 = v1.subtract(vm1).shiftRight(1);
        t1 = v1.subtract(v0);
        t2 = t2.subtract(t1).shiftRight(1);
        t1 = t1.subtract(tm1).subtract(vinf);
        t2 = t2.subtract(vinf.shiftLeft(1));
        tm1 = tm1.subtract(t2);

        // Number of bits to shift left.
        int ss = k*32;

        return vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1).shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0);
    }

    // Division

    /**
     * Returns a BigInteger whose value is {@code (this / val)}.
     *
     * @param  val value by which this BigInteger is to be divided.
     * @return {@code this / val}
     * @throws ArithmeticException if {@code val} is zero.
     */
    public BigInteger divide(BigInteger val) {
        if (val.mag.length < BURNIKEL_ZIEGLER_THRESHOLD ||
                mag.length - val.mag.length < BURNIKEL_ZIEGLER_OFFSET) {
            return divideKnuth(val);
        } else {
            return divideBurnikelZiegler(val);
        }
    }

    /**
     * Returns a BigInteger whose value is {@code (this / val)} using an O(n^2) algorithm from Knuth.
     *
     * @param  val value by which this BigInteger is to be divided.
     * @return {@code this / val}
     * @throws ArithmeticException if {@code val} is zero.
     * @see MutableBigInteger#divideKnuth(MutableBigInteger, MutableBigInteger, boolean)
     */
    private BigInteger divideKnuth(BigInteger val) {
        MutableBigInteger q = new MutableBigInteger(),
                          a = new MutableBigInteger(this.mag),
                          b = new MutableBigInteger(val.mag);

        a.divideKnuth(b, q, false);
        return q.toBigInteger(this.signum * val.signum);
    }

    /**
     * Returns an array of two BigIntegers containing {@code (this / val)}
     * followed by {@code (this % val)}.
     *
     * @param  val value by which this BigInteger is to be divided, and the
     *         remainder computed.
     * @return an array of two BigIntegers: the quotient {@code (this / val)}
     *         is the initial element, and the remainder {@code (this % val)}
     *         is the final element.
     * @throws ArithmeticException if {@code val} is zero.
     */
    public BigInteger[] divideAndRemainder(BigInteger val) {
        if (val.mag.length < BURNIKEL_ZIEGLER_THRESHOLD ||
                mag.length - val.mag.length < BURNIKEL_ZIEGLER_OFFSET) {
            return divideAndRemainderKnuth(val);
        } else {
            return divideAndRemainderBurnikelZiegler(val);
        }
    }

    /** Long division */
    private BigInteger[] divideAndRemainderKnuth(BigInteger val) {
        BigInteger[] result = new BigInteger[2];
        MutableBigInteger q = new MutableBigInteger(),
                          a = new MutableBigInteger(this.mag),
                          b = new MutableBigInteger(val.mag);
        MutableBigInteger r = a.divideKnuth(b, q);
        result[0] = q.toBigInteger(this.signum == val.signum ? 1 : -1);
        result[1] = r.toBigInteger(this.signum);
        return result;
    }

    /**
     * Returns a BigInteger whose value is {@code (this % val)}.
     *
     * @param  val value by which this BigInteger is to be divided, and the
     *         remainder computed.
     * @return {@code this % val}
     * @throws ArithmeticException if {@code val} is zero.
     */
    public BigInteger remainder(BigInteger val) {
        if (val.mag.length < BURNIKEL_ZIEGLER_THRESHOLD ||
                mag.length - val.mag.length < BURNIKEL_ZIEGLER_OFFSET) {
            return remainderKnuth(val);
        } else {
            return remainderBurnikelZiegler(val);
        }
    }

    /** Long division */
    private BigInteger remainderKnuth(BigInteger val) {
        MutableBigInteger q = new MutableBigInteger(),
                          a = new MutableBigInteger(this.mag),
                          b = new MutableBigInteger(val.mag);

        return a.divideKnuth(b, q).toBigInteger(this.signum);
    }

    /**
     * Calculates {@code this / val} using the Burnikel-Ziegler algorithm.
     * @param  val the divisor
     * @return {@code this / val}
     */
    private BigInteger divideBurnikelZiegler(BigInteger val) {
        return divideAndRemainderBurnikelZiegler(val)[0];
    }

    /**
     * Calculates {@code this % val} using the Burnikel-Ziegler algorithm.
     * @param val the divisor
     * @return {@code this % val}
     */
    private BigInteger remainderBurnikelZiegler(BigInteger val) {
        return divideAndRemainderBurnikelZiegler(val)[1];
    }

    /**
     * Computes {@code this / val} and {@code this % val} using the
     * Burnikel-Ziegler algorithm.
     * @param val the divisor
     * @return an array containing the quotient and remainder
     */
    private BigInteger[] divideAndRemainderBurnikelZiegler(BigInteger val) {
        MutableBigInteger q = new MutableBigInteger();
        MutableBigInteger r = new MutableBigInteger(this).divideAndRemainderBurnikelZiegler(new MutableBigInteger(val), q);
        BigInteger qBigInt = q.isZero() ? ZERO : q.toBigInteger(signum*val.signum);
        BigInteger rBigInt = r.isZero() ? ZERO : r.toBigInteger(signum);
        return new BigInteger[] {qBigInt, rBigInt};
    }

    /**
     * Returns a BigInteger whose value is <tt>(this<sup>exponent</sup>)</tt>.
     * Note that {@code exponent} is an integer rather than a BigInteger.
     *
     * @param  exponent exponent to which this BigInteger is to be raised.
     * @return <tt>this<sup>exponent</sup></tt>
     * @throws ArithmeticException {@code exponent} is negative.  (This would
     *         cause the operation to yield a non-integer value.)
     */
    public BigInteger pow(int exponent) {
        if (exponent < 0) {
            throw new ArithmeticException("Negative exponent");
        }
        if (signum == 0) {
            return (exponent == 0 ? ONE : this);
        }

        BigInteger partToSquare = this.abs();

        // Factor out powers of two from the base, as the exponentiation of
        // these can be done by left shifts only.
        // The remaining part can then be exponentiated faster.  The
        // powers of two will be multiplied back at the end.
        int powersOfTwo = partToSquare.getLowestSetBit();
        long bitsToShiftLong = (long)powersOfTwo * exponent;
        if (bitsToShiftLong > Integer.MAX_VALUE) {
            reportOverflow();
        }
        int bitsToShift = (int)bitsToShiftLong;

        int remainingBits;

        // Factor the powers of two out quickly by shifting right, if needed.
        if (powersOfTwo > 0) {
            partToSquare = partToSquare.shiftRight(powersOfTwo);
            remainingBits = partToSquare.bitLength();
            if (remainingBits == 1) {  // Nothing left but +/- 1?
                if (signum < 0 && (exponent&1) == 1) {
                    return NEGATIVE_ONE.shiftLeft(bitsToShift);
                } else {
                    return ONE.shiftLeft(bitsToShift);
                }
            }
        } else {
            remainingBits = partToSquare.bitLength();
            if (remainingBits == 1) { // Nothing left but +/- 1?
                if (signum < 0  && (exponent&1) == 1) {
                    return NEGATIVE_ONE;
                } else {
                    return ONE;
                }
            }
        }

        // This is a quick way to approximate the size of the result,
        // similar to doing log2[n] * exponent.  This will give an upper bound
        // of how big the result can be, and which algorithm to use.
        long scaleFactor = (long)remainingBits * exponent;

        // Use slightly different algorithms for small and large operands.
        // See if the result will safely fit into a long. (Largest 2^63-1)
        if (partToSquare.mag.length == 1 && scaleFactor <= 62) {
            // Small number algorithm.  Everything fits into a long.
            int newSign = (signum <0  && (exponent&1) == 1 ? -1 : 1);
            long result = 1;
            long baseToPow2 = partToSquare.mag[0] & LONG_MASK;

            int workingExponent = exponent;

            // Perform exponentiation using repeated squaring trick
            while (workingExponent != 0) {
                if ((workingExponent & 1) == 1) {
                    result = result * baseToPow2;
                }

                if ((workingExponent >>>= 1) != 0) {
                    baseToPow2 = baseToPow2 * baseToPow2;
                }
            }

            // Multiply back the powers of two (quickly, by shifting left)
            if (powersOfTwo > 0) {
                if (bitsToShift + scaleFactor <= 62) { // Fits in long?
                    return valueOf((result << bitsToShift) * newSign);
                } else {
                    return valueOf(result*newSign).shiftLeft(bitsToShift);
                }
            } else {
                return valueOf(result*newSign);
            }
        } else {
            if ((long)bitLength() * exponent / Integer.SIZE > MAX_MAG_LENGTH) {
                reportOverflow();
            }

            // Large number algorithm.  This is basically identical to
            // the algorithm above, but calls multiply() and square()
            // which may use more efficient algorithms for large numbers.
            BigInteger answer = ONE;

            int workingExponent = exponent;
            // Perform exponentiation using repeated squaring trick
            while (workingExponent != 0) {
                if ((workingExponent & 1) == 1) {
                    answer = answer.multiply(partToSquare);
                }

                if ((workingExponent >>>= 1) != 0) {
                    partToSquare = partToSquare.square();
                }
            }
            // Multiply back the (exponentiated) powers of two (quickly,
            // by shifting left)
            if (powersOfTwo > 0) {
                answer = answer.shiftLeft(bitsToShift);
            }

            if (signum < 0 && (exponent&1) == 1) {
                return answer.negate();
            } else {
                return answer;
            }
        }
    }

    /**
     * Returns a BigInteger whose value is the greatest common divisor of
     * {@code abs(this)} and {@code abs(val)}.  Returns 0 if
     * {@code this == 0 && val == 0}.
     *
     * @param  val value with which the GCD is to be computed.
     * @return {@code GCD(abs(this), abs(val))}
     */
    public BigInteger gcd(BigInteger val) {
        if (val.signum == 0)
            return this.abs();
        else if (this.signum == 0)
            return val.abs();

        MutableBigInteger a = new MutableBigInteger(this);
        MutableBigInteger b = new MutableBigInteger(val);

        MutableBigInteger result = a.hybridGCD(b);

        return result.toBigInteger(1);
    }

    /**
     * Package private method to return bit length for an integer.
     */
    static int bitLengthForInt(int n) {
        return 32 - Integer.numberOfLeadingZeros(n);
    }

    /**
     * Left shift int array a up to len by n bits. Returns the array that
     * results from the shift since space may have to be reallocated.
     */
    private static int[] leftShift(int[] a, int len, int n) {
        int nInts = n >>> 5;
        int nBits = n&0x1F;
        int bitsInHighWord = bitLengthForInt(a[0]);

        // If shift can be done without recopy, do so
        if (n <= (32-bitsInHighWord)) {
            primitiveLeftShift(a, len, nBits);
            return a;
        } else { // Array must be resized
            if (nBits <= (32-bitsInHighWord)) {
                int result[] = new int[nInts+len];
                System.arraycopy(a, 0, result, 0, len);
                primitiveLeftShift(result, result.length, nBits);
                return result;
            } else {
                int result[] = new int[nInts+len+1];
                System.arraycopy(a, 0, result, 0, len);
                primitiveRightShift(result, result.length, 32 - nBits);
                return result;
            }
        }
    }

    // shifts a up to len right n bits assumes no leading zeros, 0<n<32
    static void primitiveRightShift(int[] a, int len, int n) {
        int n2 = 32 - n;
        for (int i=len-1, c=a[i]; i > 0; i--) {
            int b = c;
            c = a[i-1];
            a[i] = (c << n2) | (b >>> n);
        }
        a[0] >>>= n;
    }

    // shifts a up to len left n bits assumes no leading zeros, 0<=n<32
    static void primitiveLeftShift(int[] a, int len, int n) {
        if (len == 0 || n == 0)
            return;

        int n2 = 32 - n;
        for (int i=0, c=a[i], m=i+len-1; i < m; i++) {
            int b = c;
            c = a[i+1];
            a[i] = (b << n) | (c >>> n2);
        }
        a[len-1] <<= n;
    }

    /**
     * Calculate bitlength of contents of the first len elements an int array,
     * assuming there are no leading zero ints.
     */
    private static int bitLength(int[] val, int len) {
        if (len == 0)
            return 0;
        return ((len - 1) << 5) + bitLengthForInt(val[0]);
    }

    /**
     * Returns a BigInteger whose value is the absolute value of this
     * BigInteger.
     *
     * @return {@code abs(this)}
     */
    public BigInteger abs() {
        return (signum >= 0 ? this : this.negate());
    }

    /**
     * Returns a BigInteger whose value is {@code (-this)}.
     *
     * @return {@code -this}
     */
    public BigInteger negate() {
        return new BigInteger(this.mag, -this.signum);
    }

    /**
     * Returns the signum function of this BigInteger.
     *
     * @return -1, 0 or 1 as the value of this BigInteger is negative, zero or
     *         positive.
     */
    public int signum() {
        return this.signum;
    }

    // Modular Arithmetic Operations

    /**
     * Returns a BigInteger whose value is {@code (this mod m}).  This method
     * differs from {@code remainder} in that it always returns a
     * <i>non-negative</i> BigInteger.
     *
     * @param  m the modulus.
     * @return {@code this mod m}
     * @throws ArithmeticException {@code m} &le; 0
     * @see    #remainder
     */
    public BigInteger mod(BigInteger m) {
        if (m.signum <= 0)
            throw new ArithmeticException("BigInteger: modulus not positive");

        BigInteger result = this.remainder(m);
        return (result.signum >= 0 ? result : result.add(m));
    }

    /**
     * Returns a BigInteger whose value is
     * <tt>(this<sup>exponent</sup> mod m)</tt>.  (Unlike {@code pow}, this
     * method permits negative exponents.)
     *
     * @param  exponent the exponent.
     * @param  m the modulus.
     * @return <tt>this<sup>exponent</sup> mod m</tt>
     * @throws ArithmeticException {@code m} &le; 0 or the exponent is
     *         negative and this BigInteger is not <i>relatively
     *         prime</i> to {@code m}.
     * @see    #modInverse
     */
    public BigInteger modPow(BigInteger exponent, BigInteger m) {
        if (m.signum <= 0)
            throw new ArithmeticException("BigInteger: modulus not positive");

        // Trivial cases
        if (exponent.signum == 0)
            return (m.equals(ONE) ? ZERO : ONE);

        if (this.equals(ONE))
            return (m.equals(ONE) ? ZERO : ONE);

        if (this.equals(ZERO) && exponent.signum >= 0)
            return ZERO;

        if (this.equals(negConst[1]) && (!exponent.testBit(0)))
            return (m.equals(ONE) ? ZERO : ONE);

        boolean invertResult;
        if ((invertResult = (exponent.signum < 0)))
            exponent = exponent.negate();

        BigInteger base = (this.signum < 0 || this.compareTo(m) >= 0
                           ? this.mod(m) : this);
        BigInteger result;
        if (m.testBit(0)) { // odd modulus
            result = base.oddModPow(exponent, m);
        } else {
            /*
             * Even modulus.  Tear it into an "odd part" (m1) and power of two
             * (m2), exponentiate mod m1, manually exponentiate mod m2, and
             * use Chinese Remainder Theorem to combine results.
             */

            // Tear m apart into odd part (m1) and power of 2 (m2)
            int p = m.getLowestSetBit();   // Max pow of 2 that divides m

            BigInteger m1 = m.shiftRight(p);  // m/2**p
            BigInteger m2 = ONE.shiftLeft(p); // 2**p

            // Calculate new base from m1
            BigInteger base2 = (this.signum < 0 || this.compareTo(m1) >= 0
                                ? this.mod(m1) : this);

            // Caculate (base ** exponent) mod m1.
            BigInteger a1 = (m1.equals(ONE) ? ZERO :
                             base2.oddModPow(exponent, m1));

            // Calculate (this ** exponent) mod m2
            BigInteger a2 = base.modPow2(exponent, p);

            // Combine results using Chinese Remainder Theorem
            BigInteger y1 = m2.modInverse(m1);
            BigInteger y2 = m1.modInverse(m2);

            if (m.mag.length < MAX_MAG_LENGTH / 2) {
                result = a1.multiply(m2).multiply(y1).add(a2.multiply(m1).multiply(y2)).mod(m);
            } else {
                MutableBigInteger t1 = new MutableBigInteger();
                new MutableBigInteger(a1.multiply(m2)).multiply(new MutableBigInteger(y1), t1);
                MutableBigInteger t2 = new MutableBigInteger();
                new MutableBigInteger(a2.multiply(m1)).multiply(new MutableBigInteger(y2), t2);
                t1.add(t2);
                MutableBigInteger q = new MutableBigInteger();
                result = t1.divide(new MutableBigInteger(m), q).toBigInteger();
            }
        }

        return (invertResult ? result.modInverse(m) : result);
    }

    // Montgomery multiplication.  These are wrappers for
    // implMontgomeryXX routines which are expected to be replaced by
    // virtual machine intrinsics.  We don't use the intrinsics for
    // very large operands: MONTGOMERY_INTRINSIC_THRESHOLD should be
    // larger than any reasonable crypto key.
    private static int[] montgomeryMultiply(int[] a, int[] b, int[] n, int len, long inv,
                                            int[] product) {
        implMontgomeryMultiplyChecks(a, b, n, len, product);
        if (len > MONTGOMERY_INTRINSIC_THRESHOLD) {
            // Very long argument: do not use an intrinsic
            product = multiplyToLen(a, len, b, len, product);
            return montReduce(product, n, len, (int)inv);
        } else {
            return implMontgomeryMultiply(a, b, n, len, inv, materialize(product, len));
        }
    }
    private static int[] montgomerySquare(int[] a, int[] n, int len, long inv,
                                          int[] product) {
        implMontgomeryMultiplyChecks(a, a, n, len, product);
        if (len > MONTGOMERY_INTRINSIC_THRESHOLD) {
            // Very long argument: do not use an intrinsic
            product = squareToLen(a, len, product);
            return montReduce(product, n, len, (int)inv);
        } else {
            return implMontgomerySquare(a, n, len, inv, materialize(product, len));
        }
    }

    // Range-check everything.
    private static void implMontgomeryMultiplyChecks
        (int[] a, int[] b, int[] n, int len, int[] product) throws RuntimeException {
        if (len % 2 != 0) {
            throw new IllegalArgumentException("input array length must be even: " + len);
        }

        if (len < 1) {
            throw new IllegalArgumentException("invalid input length: " + len);
        }

        if (len > a.length ||
            len > b.length ||
            len > n.length ||
            (product != null && len > product.length)) {
            throw new IllegalArgumentException("input array length out of bound: " + len);
        }
    }

    // Make sure that the int array z (which is expected to contain
    // the result of a Montgomery multiplication) is present and
    // sufficiently large.
    private static int[] materialize(int[] z, int len) {
         if (z == null || z.length < len)
             z = new int[len];
         return z;
    }

    // These methods are intended to be be replaced by virtual machine
    // intrinsics.
    private static int[] implMontgomeryMultiply(int[] a, int[] b, int[] n, int len,
                                         long inv, int[] product) {
        product = multiplyToLen(a, len, b, len, product);
        return montReduce(product, n, len, (int)inv);
    }
    private static int[] implMontgomerySquare(int[] a, int[] n, int len,
                                       long inv, int[] product) {
        product = squareToLen(a, len, product);
        return montReduce(product, n, len, (int)inv);
    }

    static int[] bnExpModThreshTable = {7, 25, 81, 241, 673, 1793,
                                                Integer.MAX_VALUE}; // Sentinel

    /**
     * Returns a BigInteger whose value is x to the power of y mod z.
     * Assumes: z is odd && x < z.
     */
    private BigInteger oddModPow(BigInteger y, BigInteger z) {
    /*
     * The algorithm is adapted from Colin Plumb's C library.
     *
     * The window algorithm:
     * The idea is to keep a running product of b1 = n^(high-order bits of exp)
     * and then keep appending exponent bits to it.  The following patterns
     * apply to a 3-bit window (k = 3):
     * To append   0: square
     * To append   1: square, multiply by n^1
     * To append  10: square, multiply by n^1, square
     * To append  11: square, square, multiply by n^3
     * To append 100: square, multiply by n^1, square, square
     * To append 101: square, square, square, multiply by n^5
     * To append 110: square, square, multiply by n^3, square
     * To append 111: square, square, square, multiply by n^7
     *
     * Since each pattern involves only one multiply, the longer the pattern
     * the better, except that a 0 (no multiplies) can be appended directly.
     * We precompute a table of odd powers of n, up to 2^k, and can then
     * multiply k bits of exponent at a time.  Actually, assuming random
     * exponents, there is on average one zero bit between needs to
     * multiply (1/2 of the time there's none, 1/4 of the time there's 1,
     * 1/8 of the time, there's 2, 1/32 of the time, there's 3, etc.), so
     * you have to do one multiply per k+1 bits of exponent.
     *
     * The loop walks down the exponent, squaring the result buffer as
     * it goes.  There is a wbits+1 bit lookahead buffer, buf, that is
     * filled with the upcoming exponent bits.  (What is read after the
     * end of the exponent is unimportant, but it is filled with zero here.)
     * When the most-significant bit of this buffer becomes set, i.e.
     * (buf & tblmask) != 0, we have to decide what pattern to multiply
     * by, and when to do it.  We decide, remember to do it in future
     * after a suitable number of squarings have passed (e.g. a pattern
     * of "100" in the buffer requires that we multiply by n^1 immediately;
     * a pattern of "110" calls for multiplying by n^3 after one more
     * squaring), clear the buffer, and continue.
     *
     * When we start, there is one more optimization: the result buffer
     * is implcitly one, so squaring it or multiplying by it can be
     * optimized away.  Further, if we start with a pattern like "100"
     * in the lookahead window, rather than placing n into the buffer
     * and then starting to square it, we have already computed n^2
     * to compute the odd-powers table, so we can place that into
     * the buffer and save a squaring.
     *
     * This means that if you have a k-bit window, to compute n^z,
     * where z is the high k bits of the exponent, 1/2 of the time
     * it requires no squarings.  1/4 of the time, it requires 1
     * squaring, ... 1/2^(k-1) of the time, it reqires k-2 squarings.
     * And the remaining 1/2^(k-1) of the time, the top k bits are a
     * 1 followed by k-1 0 bits, so it again only requires k-2
     * squarings, not k-1.  The average of these is 1.  Add that
     * to the one squaring we have to do to compute the table,
     * and you'll see that a k-bit window saves k-2 squarings
     * as well as reducing the multiplies.  (It actually doesn't
     * hurt in the case k = 1, either.)
     */
        // Special case for exponent of one
        if (y.equals(ONE))
            return this;

        // Special case for base of zero
        if (signum == 0)
            return ZERO;

        int[] base = mag.clone();
        int[] exp = y.mag;
        int[] mod = z.mag;
        int modLen = mod.length;

        // Make modLen even. It is conventional to use a cryptographic
        // modulus that is 512, 768, 1024, or 2048 bits, so this code
        // will not normally be executed. However, it is necessary for
        // the correct functioning of the HotSpot intrinsics.
        if ((modLen & 1) != 0) {
            int[] x = new int[modLen + 1];
            System.arraycopy(mod, 0, x, 1, modLen);
            mod = x;
            modLen++;
        }

        // Select an appropriate window size
        int wbits = 0;
        int ebits = bitLength(exp, exp.length);
        // if exponent is 65537 (0x10001), use minimum window size
        if ((ebits != 17) || (exp[0] != 65537)) {
            while (ebits > bnExpModThreshTable[wbits]) {
                wbits++;
            }
        }

        // Calculate appropriate table size
        int tblmask = 1 << wbits;

        // Allocate table for precomputed odd powers of base in Montgomery form
        int[][] table = new int[tblmask][];
        for (int i=0; i < tblmask; i++)
            table[i] = new int[modLen];

        // Compute the modular inverse of the least significant 64-bit
        // digit of the modulus
        long n0 = (mod[modLen-1] & LONG_MASK) + ((mod[modLen-2] & LONG_MASK) << 32);
        long inv = -MutableBigInteger.inverseMod64(n0);

        // Convert base to Montgomery form
        int[] a = leftShift(base, base.length, modLen << 5);

        MutableBigInteger q = new MutableBigInteger(),
                          a2 = new MutableBigInteger(a),
                          b2 = new MutableBigInteger(mod);
        b2.normalize(); // MutableBigInteger.divide() assumes that its
                        // divisor is in normal form.

        MutableBigInteger r= a2.divide(b2, q);
        table[0] = r.toIntArray();

        // Pad table[0] with leading zeros so its length is at least modLen
        if (table[0].length < modLen) {
           int offset = modLen - table[0].length;
           int[] t2 = new int[modLen];
           System.arraycopy(table[0], 0, t2, offset, table[0].length);
           table[0] = t2;
        }

        // Set b to the square of the base
        int[] b = montgomerySquare(table[0], mod, modLen, inv, null);

        // Set t to high half of b
        int[] t = Arrays.copyOf(b, modLen);

        // Fill in the table with odd powers of the base
        for (int i=1; i < tblmask; i++) {
            table[i] = montgomeryMultiply(t, table[i-1], mod, modLen, inv, null);
        }

        // Pre load the window that slides over the exponent
        int bitpos = 1 << ((ebits-1) & (32-1));

        int buf = 0;
        int elen = exp.length;
        int eIndex = 0;
        for (int i = 0; i <= wbits; i++) {
            buf = (buf << 1) | (((exp[eIndex] & bitpos) != 0)?1:0);
            bitpos >>>= 1;
            if (bitpos == 0) {
                eIndex++;
                bitpos = 1 << (32-1);
                elen--;
            }
        }

        int multpos = ebits;

        // The first iteration, which is hoisted out of the main loop
        ebits--;
        boolean isone = true;

        multpos = ebits - wbits;
        while ((buf & 1) == 0) {
            buf >>>= 1;
            multpos++;
        }

        int[] mult = table[buf >>> 1];

        buf = 0;
        if (multpos == ebits)
            isone = false;

        // The main loop
        while (true) {
            ebits--;
            // Advance the window
            buf <<= 1;

            if (elen != 0) {
                buf |= ((exp[eIndex] & bitpos) != 0) ? 1 : 0;
                bitpos >>>= 1;
                if (bitpos == 0) {
                    eIndex++;
                    bitpos = 1 << (32-1);
                    elen--;
                }
            }

            // Examine the window for pending multiplies
            if ((buf & tblmask) != 0) {
                multpos = ebits - wbits;
                while ((buf & 1) == 0) {
                    buf >>>= 1;
                    multpos++;
                }
                mult = table[buf >>> 1];
                buf = 0;
            }

            // Perform multiply
            if (ebits == multpos) {
                if (isone) {
                    b = mult.clone();
                    isone = false;
                } else {
                    t = b;
                    a = montgomeryMultiply(t, mult, mod, modLen, inv, a);
                    t = a; a = b; b = t;
                }
            }

            // Check if done
            if (ebits == 0)
                break;

            // Square the input
            if (!isone) {
                t = b;
                a = montgomerySquare(t, mod, modLen, inv, a);
                t = a; a = b; b = t;
            }
        }

        // Convert result out of Montgomery form and return
        int[] t2 = new int[2*modLen];
        System.arraycopy(b, 0, t2, modLen, modLen);

        b = montReduce(t2, mod, modLen, (int)inv);

        t2 = Arrays.copyOf(b, modLen);

        return new BigInteger(1, t2);
    }

    /**
     * Montgomery reduce n, modulo mod.  This reduces modulo mod and divides
     * by 2^(32*mlen). Adapted from Colin Plumb's C library.
     */
    private static int[] montReduce(int[] n, int[] mod, int mlen, int inv) {
        int c=0;
        int len = mlen;
        int offset=0;

        do {
            int nEnd = n[n.length-1-offset];
            int carry = mulAdd(n, mod, offset, mlen, inv * nEnd);
            c += addOne(n, offset, mlen, carry);
            offset++;
        } while (--len > 0);

        while (c > 0)
            c += subN(n, mod, mlen);

        while (intArrayCmpToLen(n, mod, mlen) >= 0)
            subN(n, mod, mlen);

        return n;
    }


    /*
     * Returns -1, 0 or +1 as big-endian unsigned int array arg1 is less than,
     * equal to, or greater than arg2 up to length len.
     */
    private static int intArrayCmpToLen(int[] arg1, int[] arg2, int len) {
        for (int i=0; i < len; i++) {
            long b1 = arg1[i] & LONG_MASK;
            long b2 = arg2[i] & LONG_MASK;
            if (b1 < b2)
                return -1;
            if (b1 > b2)
                return 1;
        }
        return 0;
    }

    /**
     * Subtracts two numbers of same length, returning borrow.
     */
    private static int subN(int[] a, int[] b, int len) {
        long sum = 0;

        while (--len >= 0) {
            sum = (a[len] & LONG_MASK) -
                 (b[len] & LONG_MASK) + (sum >> 32);
            a[len] = (int)sum;
        }

        return (int)(sum >> 32);
    }

    /**
     * Multiply an array by one word k and add to result, return the carry
     */
    static int mulAdd(int[] out, int[] in, int offset, int len, int k) {
        implMulAddCheck(out, in, offset, len, k);
        return implMulAdd(out, in, offset, len, k);
    }

    /**
     * Parameters validation.
     */
    private static void implMulAddCheck(int[] out, int[] in, int offset, int len, int k) {
        if (len > in.length) {
            throw new IllegalArgumentException("input length is out of bound: " + len + " > " + in.length);
        }
        if (offset < 0) {
            throw new IllegalArgumentException("input offset is invalid: " + offset);
        }
        if (offset > (out.length - 1)) {
            throw new IllegalArgumentException("input offset is out of bound: " + offset + " > " + (out.length - 1));
        }
        if (len > (out.length - offset)) {
            throw new IllegalArgumentException("input len is out of bound: " + len + " > " + (out.length - offset));
        }
    }

    /**
     * Java Runtime may use intrinsic for this method.
     */
    private static int implMulAdd(int[] out, int[] in, int offset, int len, int k) {
        long kLong = k & LONG_MASK;
        long carry = 0;

        offset = out.length-offset - 1;
        for (int j=len-1; j >= 0; j--) {
            long product = (in[j] & LONG_MASK) * kLong +
                           (out[offset] & LONG_MASK) + carry;
            out[offset--] = (int)product;
            carry = product >>> 32;
        }
        return (int)carry;
    }

    /**
     * Add one word to the number a mlen words into a. Return the resulting
     * carry.
     */
    static int addOne(int[] a, int offset, int mlen, int carry) {
        offset = a.length-1-mlen-offset;
        long t = (a[offset] & LONG_MASK) + (carry & LONG_MASK);

        a[offset] = (int)t;
        if ((t >>> 32) == 0)
            return 0;
        while (--mlen >= 0) {
            if (--offset < 0) { // Carry out of number
                return 1;
            } else {
                a[offset]++;
                if (a[offset] != 0)
                    return 0;
            }
        }
        return 1;
    }

    /**
     * Returns a BigInteger whose value is (this ** exponent) mod (2**p)
     */
    private BigInteger modPow2(BigInteger exponent, int p) {
        /*
         * Perform exponentiation using repeated squaring trick, chopping off
         * high order bits as indicated by modulus.
         */
        BigInteger result = ONE;
        BigInteger baseToPow2 = this.mod2(p);
        int expOffset = 0;

        int limit = exponent.bitLength();

        if (this.testBit(0))
           limit = (p-1) < limit ? (p-1) : limit;

        while (expOffset < limit) {
            if (exponent.testBit(expOffset))
                result = result.multiply(baseToPow2).mod2(p);
            expOffset++;
            if (expOffset < limit)
                baseToPow2 = baseToPow2.square().mod2(p);
        }

        return result;
    }

    /**
     * Returns a BigInteger whose value is this mod(2**p).
     * Assumes that this {@code BigInteger >= 0} and {@code p > 0}.
     */
    private BigInteger mod2(int p) {
        if (bitLength() <= p)
            return this;

        // Copy remaining ints of mag
        int numInts = (p + 31) >>> 5;
        int[] mag = new int[numInts];
        System.arraycopy(this.mag, (this.mag.length - numInts), mag, 0, numInts);

        // Mask out any excess bits
        int excessBits = (numInts << 5) - p;
        mag[0] &= (1L << (32-excessBits)) - 1;

        return (mag[0] == 0 ? new BigInteger(1, mag) : new BigInteger(mag, 1));
    }

    /**
     * Returns a BigInteger whose value is {@code (this}<sup>-1</sup> {@code mod m)}.
     *
     * @param  m the modulus.
     * @return {@code this}<sup>-1</sup> {@code mod m}.
     * @throws ArithmeticException {@code  m} &le; 0, or this BigInteger
     *         has no multiplicative inverse mod m (that is, this BigInteger
     *         is not <i>relatively prime</i> to m).
     */
    public BigInteger modInverse(BigInteger m) {
        if (m.signum != 1)
            throw new ArithmeticException("BigInteger: modulus not positive");

        if (m.equals(ONE))
            return ZERO;

        // Calculate (this mod m)
        BigInteger modVal = this;
        if (signum < 0 || (this.compareMagnitude(m) >= 0))
            modVal = this.mod(m);

        if (modVal.equals(ONE))
            return ONE;

        MutableBigInteger a = new MutableBigInteger(modVal);
        MutableBigInteger b = new MutableBigInteger(m);

        MutableBigInteger result = a.mutableModInverse(b);
        return result.toBigInteger(1);
    }

    // Shift Operations

    /**
     * Returns a BigInteger whose value is {@code (this << n)}.
     * The shift distance, {@code n}, may be negative, in which case
     * this method performs a right shift.
     * (Computes <tt>floor(this * 2<sup>n</sup>)</tt>.)
     *
     * @param  n shift distance, in bits.
     * @return {@code this << n}
     * @see #shiftRight
     */
    public BigInteger shiftLeft(int n) {
        if (signum == 0)
            return ZERO;
        if (n > 0) {
            return new BigInteger(shiftLeft(mag, n), signum);
        } else if (n == 0) {
            return this;
        } else {
            // Possible int overflow in (-n) is not a trouble,
            // because shiftRightImpl considers its argument unsigned
            return shiftRightImpl(-n);
        }
    }

    /**
     * Returns a magnitude array whose value is {@code (mag << n)}.
     * The shift distance, {@code n}, is considered unnsigned.
     * (Computes <tt>this * 2<sup>n</sup></tt>.)
     *
     * @param mag magnitude, the most-significant int ({@code mag[0]}) must be non-zero.
     * @param  n unsigned shift distance, in bits.
     * @return {@code mag << n}
     */
    private static int[] shiftLeft(int[] mag, int n) {
        int nInts = n >>> 5;
        int nBits = n & 0x1f;
        int magLen = mag.length;
        int newMag[] = null;

        if (nBits == 0) {
            newMag = new int[magLen + nInts];
            System.arraycopy(mag, 0, newMag, 0, magLen);
        } else {
            int i = 0;
            int nBits2 = 32 - nBits;
            int highBits = mag[0] >>> nBits2;
            if (highBits != 0) {
                newMag = new int[magLen + nInts + 1];
                newMag[i++] = highBits;
            } else {
                newMag = new int[magLen + nInts];
            }
            int j=0;
            while (j < magLen-1)
                newMag[i++] = mag[j++] << nBits | mag[j] >>> nBits2;
            newMag[i] = mag[j] << nBits;
        }
        return newMag;
    }

    /**
     * Returns a BigInteger whose value is {@code (this >> n)}.  Sign
     * extension is performed.  The shift distance, {@code n}, may be
     * negative, in which case this method performs a left shift.
     * (Computes <tt>floor(this / 2<sup>n</sup>)</tt>.)
     *
     * @param  n shift distance, in bits.
     * @return {@code this >> n}
     * @see #shiftLeft
     */
    public BigInteger shiftRight(int n) {
        if (signum == 0)
            return ZERO;
        if (n > 0) {
            return shiftRightImpl(n);
        } else if (n == 0) {
            return this;
        } else {
            // Possible int overflow in {@code -n} is not a trouble,
            // because shiftLeft considers its argument unsigned
            return new BigInteger(shiftLeft(mag, -n), signum);
        }
    }

    /**
     * Returns a BigInteger whose value is {@code (this >> n)}. The shift
     * distance, {@code n}, is considered unsigned.
     * (Computes <tt>floor(this * 2<sup>-n</sup>)</tt>.)
     *
     * @param  n unsigned shift distance, in bits.
     * @return {@code this >> n}
     */
    private BigInteger shiftRightImpl(int n) {
        int nInts = n >>> 5;
        int nBits = n & 0x1f;
        int magLen = mag.length;
        int newMag[] = null;

        // Special case: entire contents shifted off the end
        if (nInts >= magLen)
            return (signum >= 0 ? ZERO : negConst[1]);

        if (nBits == 0) {
            int newMagLen = magLen - nInts;
            newMag = Arrays.copyOf(mag, newMagLen);
        } else {
            int i = 0;
            int highBits = mag[0] >>> nBits;
            if (highBits != 0) {
                newMag = new int[magLen - nInts];
                newMag[i++] = highBits;
            } else {
                newMag = new int[magLen - nInts -1];
            }

            int nBits2 = 32 - nBits;
            int j=0;
            while (j < magLen - nInts - 1)
                newMag[i++] = (mag[j++] << nBits2) | (mag[j] >>> nBits);
        }

        if (signum < 0) {
            // Find out whether any one-bits were shifted off the end.
            boolean onesLost = false;
            for (int i=magLen-1, j=magLen-nInts; i >= j && !onesLost; i--)
                onesLost = (mag[i] != 0);
            if (!onesLost && nBits != 0)
                onesLost = (mag[magLen - nInts - 1] << (32 - nBits) != 0);

            if (onesLost)
                newMag = javaIncrement(newMag);
        }

        return new BigInteger(newMag, signum);
    }

    int[] javaIncrement(int[] val) {
        int lastSum = 0;
        for (int i=val.length-1;  i >= 0 && lastSum == 0; i--)
            lastSum = (val[i] += 1);
        if (lastSum == 0) {
            val = new int[val.length+1];
            val[0] = 1;
        }
        return val;
    }

    // Bitwise Operations

    /**
     * Returns a BigInteger whose value is {@code (this & val)}.  (This
     * method returns a negative BigInteger if and only if this and val are
     * both negative.)
     *
     * @param val value to be AND'ed with this BigInteger.
     * @return {@code this & val}
     */
    public BigInteger and(BigInteger val) {
        int[] result = new int[Math.max(intLength(), val.intLength())];
        for (int i=0; i < result.length; i++)
            result[i] = (getInt(result.length-i-1)
                         & val.getInt(result.length-i-1));

        return valueOf(result);
    }

    /**
     * Returns a BigInteger whose value is {@code (this | val)}.  (This method
     * returns a negative BigInteger if and only if either this or val is
     * negative.)
     *
     * @param val value to be OR'ed with this BigInteger.
     * @return {@code this | val}
     */
    public BigInteger or(BigInteger val) {
        int[] result = new int[Math.max(intLength(), val.intLength())];
        for (int i=0; i < result.length; i++)
            result[i] = (getInt(result.length-i-1)
                         | val.getInt(result.length-i-1));

        return valueOf(result);
    }

    /**
     * Returns a BigInteger whose value is {@code (this ^ val)}.  (This method
     * returns a negative BigInteger if and only if exactly one of this and
     * val are negative.)
     *
     * @param val value to be XOR'ed with this BigInteger.
     * @return {@code this ^ val}
     */
    public BigInteger xor(BigInteger val) {
        int[] result = new int[Math.max(intLength(), val.intLength())];
        for (int i=0; i < result.length; i++)
            result[i] = (getInt(result.length-i-1)
                         ^ val.getInt(result.length-i-1));

        return valueOf(result);
    }

    /**
     * Returns a BigInteger whose value is {@code (~this)}.  (This method
     * returns a negative value if and only if this BigInteger is
     * non-negative.)
     *
     * @return {@code ~this}
     */
    public BigInteger not() {
        int[] result = new int[intLength()];
        for (int i=0; i < result.length; i++)
            result[i] = ~getInt(result.length-i-1);

        return valueOf(result);
    }

    /**
     * Returns a BigInteger whose value is {@code (this & ~val)}.  This
     * method, which is equivalent to {@code and(val.not())}, is provided as
     * a convenience for masking operations.  (This method returns a negative
     * BigInteger if and only if {@code this} is negative and {@code val} is
     * positive.)
     *
     * @param val value to be complemented and AND'ed with this BigInteger.
     * @return {@code this & ~val}
     */
    public BigInteger andNot(BigInteger val) {
        int[] result = new int[Math.max(intLength(), val.intLength())];
        for (int i=0; i < result.length; i++)
            result[i] = (getInt(result.length-i-1)
                         & ~val.getInt(result.length-i-1));

        return valueOf(result);
    }


    // Single Bit Operations

    /**
     * Returns {@code true} if and only if the designated bit is set.
     * (Computes {@code ((this & (1<<n)) != 0)}.)
     *
     * @param  n index of bit to test.
     * @return {@code true} if and only if the designated bit is set.
     * @throws ArithmeticException {@code n} is negative.
     */
    public boolean testBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");

        return (getInt(n >>> 5) & (1 << (n & 31))) != 0;
    }

    /**
     * Returns a BigInteger whose value is equivalent to this BigInteger
     * with the designated bit set.  (Computes {@code (this | (1<<n))}.)
     *
     * @param  n index of bit to set.
     * @return {@code this | (1<<n)}
     * @throws ArithmeticException {@code n} is negative.
     */
    public BigInteger setBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");

        int intNum = n >>> 5;
        int[] result = new int[Math.max(intLength(), intNum+2)];

        for (int i=0; i < result.length; i++)
            result[result.length-i-1] = getInt(i);

        result[result.length-intNum-1] |= (1 << (n & 31));

        return valueOf(result);
    }

    /**
     * Returns a BigInteger whose value is equivalent to this BigInteger
     * with the designated bit cleared.
     * (Computes {@code (this & ~(1<<n))}.)
     *
     * @param  n index of bit to clear.
     * @return {@code this & ~(1<<n)}
     * @throws ArithmeticException {@code n} is negative.
     */
    public BigInteger clearBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");

        int intNum = n >>> 5;
        int[] result = new int[Math.max(intLength(), ((n + 1) >>> 5) + 1)];

        for (int i=0; i < result.length; i++)
            result[result.length-i-1] = getInt(i);

        result[result.length-intNum-1] &= ~(1 << (n & 31));

        return valueOf(result);
    }

    /**
     * Returns a BigInteger whose value is equivalent to this BigInteger
     * with the designated bit flipped.
     * (Computes {@code (this ^ (1<<n))}.)
     *
     * @param  n index of bit to flip.
     * @return {@code this ^ (1<<n)}
     * @throws ArithmeticException {@code n} is negative.
     */
    public BigInteger flipBit(int n) {
        if (n < 0)
            throw new ArithmeticException("Negative bit address");

        int intNum = n >>> 5;
        int[] result = new int[Math.max(intLength(), intNum+2)];

        for (int i=0; i < result.length; i++)
            result[result.length-i-1] = getInt(i);

        result[result.length-intNum-1] ^= (1 << (n & 31));

        return valueOf(result);
    }

    /**
     * Returns the index of the rightmost (lowest-order) one bit in this
     * BigInteger (the number of zero bits to the right of the rightmost
     * one bit).  Returns -1 if this BigInteger contains no one bits.
     * (Computes {@code (this == 0? -1 : log2(this & -this))}.)
     *
     * @return index of the rightmost one bit in this BigInteger.
     */
    public int getLowestSetBit() {
        @SuppressWarnings("deprecation") int lsb = lowestSetBit - 2;
        if (lsb == -2) {  // lowestSetBit not initialized yet
            lsb = 0;
            if (signum == 0) {
                lsb -= 1;
            } else {
                // Search for lowest order nonzero int
                int i,b;
                for (i=0; (b = getInt(i)) == 0; i++)
                    ;
                lsb += (i << 5) + Integer.numberOfTrailingZeros(b);
            }
            lowestSetBit = lsb + 2;
        }
        return lsb;
    }


    // Miscellaneous Bit Operations

    /**
     * Returns the number of bits in the minimal two's-complement
     * representation of this BigInteger, <i>excluding</i> a sign bit.
     * For positive BigIntegers, this is equivalent to the number of bits in
     * the ordinary binary representation.  (Computes
     * {@code (ceil(log2(this < 0 ? -this : this+1)))}.)
     *
     * @return number of bits in the minimal two's-complement
     *         representation of this BigInteger, <i>excluding</i> a sign bit.
     */
    public int bitLength() {
        @SuppressWarnings("deprecation") int n = bitLength - 1;
        if (n == -1) { // bitLength not initialized yet
            int[] m = mag;
            int len = m.length;
            if (len == 0) {
                n = 0; // offset by one to initialize
            }  else {
                // Calculate the bit length of the magnitude
                int magBitLength = ((len - 1) << 5) + bitLengthForInt(mag[0]);
                 if (signum < 0) {
                     // Check if magnitude is a power of two
                     boolean pow2 = (Integer.bitCount(mag[0]) == 1);
                     for (int i=1; i< len && pow2; i++)
                         pow2 = (mag[i] == 0);

                     n = (pow2 ? magBitLength - 1 : magBitLength);
                 } else {
                     n = magBitLength;
                 }
            }
            bitLength = n + 1;
        }
        return n;
    }

    /**
     * Returns the number of bits in the two's complement representation
     * of this BigInteger that differ from its sign bit.  This method is
     * useful when implementing bit-vector style sets atop BigIntegers.
     *
     * @return number of bits in the two's complement representation
     *         of this BigInteger that differ from its sign bit.
     */
    public int bitCount() {
        @SuppressWarnings("deprecation") int bc = bitCount - 1;
        if (bc == -1) {  // bitCount not initialized yet
            bc = 0;      // offset by one to initialize
            // Count the bits in the magnitude
            for (int i=0; i < mag.length; i++)
                bc += Integer.bitCount(mag[i]);
            if (signum < 0) {
                // Count the trailing zeros in the magnitude
                int magTrailingZeroCount = 0, j;
                for (j=mag.length-1; mag[j] == 0; j--)
                    magTrailingZeroCount += 32;
                magTrailingZeroCount += Integer.numberOfTrailingZeros(mag[j]);
                bc += magTrailingZeroCount - 1;
            }
            bitCount = bc + 1;
        }
        return bc;
    }

    // Primality Testing

    /**
     * Returns {@code true} if this BigInteger is probably prime,
     * {@code false} if it's definitely composite.  If
     * {@code certainty} is &le; 0, {@code true} is
     * returned.
     *
     * @param  certainty a measure of the uncertainty that the caller is
     *         willing to tolerate: if the call returns {@code true}
     *         the probability that this BigInteger is prime exceeds
     *         (1 - 1/2<sup>{@code certainty}</sup>).  The execution time of
     *         this method is proportional to the value of this parameter.
     * @return {@code true} if this BigInteger is probably prime,
     *         {@code false} if it's definitely composite.
     */
    public boolean isProbablePrime(int certainty) {
        if (certainty <= 0)
            return true;
        BigInteger w = this.abs();
        if (w.equals(TWO))
            return true;
        if (!w.testBit(0) || w.equals(ONE))
            return false;

        return w.primeToCertainty(certainty, null);
    }

    // Comparison Operations

    /**
     * Compares this BigInteger with the specified BigInteger.  This
     * method is provided in preference to individual methods for each
     * of the six boolean comparison operators ({@literal <}, ==,
     * {@literal >}, {@literal >=}, !=, {@literal <=}).  The suggested
     * idiom for performing these comparisons is: {@code
     * (x.compareTo(y)} &lt;<i>op</i>&gt; {@code 0)}, where
     * &lt;<i>op</i>&gt; is one of the six comparison operators.
     *
     * @param  val BigInteger to which this BigInteger is to be compared.
     * @return -1, 0 or 1 as this BigInteger is numerically less than, equal
     *         to, or greater than {@code val}.
     */
    public int compareTo(BigInteger val) {
        if (signum == val.signum) {
            switch (signum) {
            case 1:
                return compareMagnitude(val);
            case -1:
                return val.compareMagnitude(this);
            default:
                return 0;
            }
        }
        return signum > val.signum ? 1 : -1;
    }

    /**
     * Compares the magnitude array of this BigInteger with the specified
     * BigInteger's. This is the version of compareTo ignoring sign.
     *
     * @param val BigInteger whose magnitude array to be compared.
     * @return -1, 0 or 1 as this magnitude array is less than, equal to or
     *         greater than the magnitude aray for the specified BigInteger's.
     */
    final int compareMagnitude(BigInteger val) {
        int[] m1 = mag;
        int len1 = m1.length;
        int[] m2 = val.mag;
        int len2 = m2.length;
        if (len1 < len2)
            return -1;
        if (len1 > len2)
            return 1;
        for (int i = 0; i < len1; i++) {
            int a = m1[i];
            int b = m2[i];
            if (a != b)
                return ((a & LONG_MASK) < (b & LONG_MASK)) ? -1 : 1;
        }
        return 0;
    }

    /**
     * Version of compareMagnitude that compares magnitude with long value.
     * val can't be Long.MIN_VALUE.
     */
    final int compareMagnitude(long val) {
        assert val != Long.MIN_VALUE;
        int[] m1 = mag;
        int len = m1.length;
        if (len > 2) {
            return 1;
        }
        if (val < 0) {
            val = -val;
        }
        int highWord = (int)(val >>> 32);
        if (highWord == 0) {
            if (len < 1)
                return -1;
            if (len > 1)
                return 1;
            int a = m1[0];
            int b = (int)val;
            if (a != b) {
                return ((a & LONG_MASK) < (b & LONG_MASK))? -1 : 1;
            }
            return 0;
        } else {
            if (len < 2)
                return -1;
            int a = m1[0];
            int b = highWord;
            if (a != b) {
                return ((a & LONG_MASK) < (b & LONG_MASK))? -1 : 1;
            }
            a = m1[1];
            b = (int)val;
            if (a != b) {
                return ((a & LONG_MASK) < (b & LONG_MASK))? -1 : 1;
            }
            return 0;
        }
    }

    /**
     * Compares this BigInteger with the specified Object for equality.
     *
     * @param  x Object to which this BigInteger is to be compared.
     * @return {@code true} if and only if the specified Object is a
     *         BigInteger whose value is numerically equal to this BigInteger.
     */
    public boolean equals(Object x) {
        // This test is just an optimization, which may or may not help
        if (x == this)
            return true;

        if (!(x instanceof BigInteger))
            return false;

        BigInteger xInt = (BigInteger) x;
        if (xInt.signum != signum)
            return false;

        int[] m = mag;
        int len = m.length;
        int[] xm = xInt.mag;
        if (len != xm.length)
            return false;

        for (int i = 0; i < len; i++)
            if (xm[i] != m[i])
                return false;

        return true;
    }

    /**
     * Returns the minimum of this BigInteger and {@code val}.
     *
     * @param  val value with which the minimum is to be computed.
     * @return the BigInteger whose value is the lesser of this BigInteger and
     *         {@code val}.  If they are equal, either may be returned.
     */
    public BigInteger min(BigInteger val) {
        return (compareTo(val) < 0 ? this : val);
    }

    /**
     * Returns the maximum of this BigInteger and {@code val}.
     *
     * @param  val value with which the maximum is to be computed.
     * @return the BigInteger whose value is the greater of this and
     *         {@code val}.  If they are equal, either may be returned.
     */
    public BigInteger max(BigInteger val) {
        return (compareTo(val) > 0 ? this : val);
    }


    // Hash Function

    /**
     * Returns the hash code for this BigInteger.
     *
     * @return hash code for this BigInteger.
     */
    public int hashCode() {
        int hashCode = 0;

        for (int i=0; i < mag.length; i++)
            hashCode = (int)(31*hashCode + (mag[i] & LONG_MASK));

        return hashCode * signum;
    }

    /**
     * Returns the String representation of this BigInteger in the
     * given radix.  If the radix is outside the range from {@link
     * Character#MIN_RADIX} to {@link Character#MAX_RADIX} inclusive,
     * it will default to 10 (as is the case for
     * {@code Integer.toString}).  The digit-to-character mapping
     * provided by {@code Character.forDigit} is used, and a minus
     * sign is prepended if appropriate.  (This representation is
     * compatible with the {@link #BigInteger(String, int) (String,
     * int)} constructor.)
     *
     * @param  radix  radix of the String representation.
     * @return String representation of this BigInteger in the given radix.
     * @see    Integer#toString
     * @see    Character#forDigit
     * @see    #BigInteger(java.lang.String, int)
     */
    public String toString(int radix) {
        if (signum == 0)
            return "0";
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;

        // If it's small enough, use smallToString.
        if (mag.length <= SCHOENHAGE_BASE_CONVERSION_THRESHOLD)
           return smallToString(radix);

        // Otherwise use recursive toString, which requires positive arguments.
        // The results will be concatenated into this StringBuilder
        StringBuilder sb = new StringBuilder();
        if (signum < 0) {
            toString(this.negate(), sb, radix, 0);
            sb.insert(0, '-');
        }
        else
            toString(this, sb, radix, 0);

        return sb.toString();
    }

    /** This method is used to perform toString when arguments are small. */
    private String smallToString(int radix) {
        if (signum == 0) {
            return "0";
        }

        // Compute upper bound on number of digit groups and allocate space
        int maxNumDigitGroups = (4*mag.length + 6)/7;
        String digitGroup[] = new String[maxNumDigitGroups];

        // Translate number to string, a digit group at a time
        BigInteger tmp = this.abs();
        int numGroups = 0;
        while (tmp.signum != 0) {
            BigInteger d = longRadix[radix];

            MutableBigInteger q = new MutableBigInteger(),
                              a = new MutableBigInteger(tmp.mag),
                              b = new MutableBigInteger(d.mag);
            MutableBigInteger r = a.divide(b, q);
            BigInteger q2 = q.toBigInteger(tmp.signum * d.signum);
            BigInteger r2 = r.toBigInteger(tmp.signum * d.signum);

            digitGroup[numGroups++] = Long.toString(r2.longValue(), radix);
            tmp = q2;
        }

        // Put sign (if any) and first digit group into result buffer
        StringBuilder buf = new StringBuilder(numGroups*digitsPerLong[radix]+1);
        if (signum < 0) {
            buf.append('-');
        }
        buf.append(digitGroup[numGroups-1]);

        // Append remaining digit groups padded with leading zeros
        for (int i=numGroups-2; i >= 0; i--) {
            // Prepend (any) leading zeros for this digit group
            int numLeadingZeros = digitsPerLong[radix]-digitGroup[i].length();
            if (numLeadingZeros != 0) {
                buf.append(zeros[numLeadingZeros]);
            }
            buf.append(digitGroup[i]);
        }
        return buf.toString();
    }

    /**
     * Converts the specified BigInteger to a string and appends to
     * {@code sb}.  This implements the recursive Schoenhage algorithm
     * for base conversions.
     * <p/>
     * See Knuth, Donald,  _The Art of Computer Programming_, Vol. 2,
     * Answers to Exercises (4.4) Question 14.
     *
     * @param u      The number to convert to a string.
     * @param sb     The StringBuilder that will be appended to in place.
     * @param radix  The base to convert to.
     * @param digits The minimum number of digits to pad to.
     */
    private static void toString(BigInteger u, StringBuilder sb, int radix,
                                 int digits) {
        /* If we're smaller than a certain threshold, use the smallToString
           method, padding with leading zeroes when necessary. */
        if (u.mag.length <= SCHOENHAGE_BASE_CONVERSION_THRESHOLD) {
            String s = u.smallToString(radix);

            // Pad with internal zeros if necessary.
            // Don't pad if we're at the beginning of the string.
            if ((s.length() < digits) && (sb.length() > 0)) {
                for (int i=s.length(); i < digits; i++) { // May be a faster way to
                    sb.append('0');                    // do this?
                }
            }

            sb.append(s);
            return;
        }

        int b, n;
        b = u.bitLength();

        // Calculate a value for n in the equation radix^(2^n) = u
        // and subtract 1 from that value.  This is used to find the
        // cache index that contains the best value to divide u.
        n = (int) Math.round(Math.log(b * LOG_TWO / logCache[radix]) / LOG_TWO - 1.0);
        BigInteger v = getRadixConversionCache(radix, n);
        BigInteger[] results;
        results = u.divideAndRemainder(v);

        int expectedDigits = 1 << n;

        // Now recursively build the two halves of each number.
        toString(results[0], sb, radix, digits-expectedDigits);
        toString(results[1], sb, radix, expectedDigits);
    }

    /**
     * Returns the value radix^(2^exponent) from the cache.
     * If this value doesn't already exist in the cache, it is added.
     * <p/>
     * This could be changed to a more complicated caching method using
     * {@code Future}.
     */
    private static BigInteger getRadixConversionCache(int radix, int exponent) {
        BigInteger[] cacheLine = powerCache[radix]; // volatile read
        if (exponent < cacheLine.length) {
            return cacheLine[exponent];
        }

        int oldLength = cacheLine.length;
        cacheLine = Arrays.copyOf(cacheLine, exponent + 1);
        for (int i = oldLength; i <= exponent; i++) {
            cacheLine[i] = cacheLine[i - 1].pow(2);
        }

        BigInteger[][] pc = powerCache; // volatile read again
        if (exponent >= pc[radix].length) {
            pc = pc.clone();
            pc[radix] = cacheLine;
            powerCache = pc; // volatile write, publish
        }
        return cacheLine[exponent];
    }

    /* zero[i] is a string of i consecutive zeros. */
    private static String zeros[] = new String[64];
    static {
        zeros[63] =
            "000000000000000000000000000000000000000000000000000000000000000";
        for (int i=0; i < 63; i++)
            zeros[i] = zeros[63].substring(0, i);
    }

    /**
     * Returns the decimal String representation of this BigInteger.
     * The digit-to-character mapping provided by
     * {@code Character.forDigit} is used, and a minus sign is
     * prepended if appropriate.  (This representation is compatible
     * with the {@link #BigInteger(String) (String)} constructor, and
     * allows for String concatenation with Java's + operator.)
     *
     * @return decimal String representation of this BigInteger.
     * @see    Character#forDigit
     * @see    #BigInteger(java.lang.String)
     */
    public String toString() {
        return toString(10);
    }

    /**
     * Returns a byte array containing the two's-complement
     * representation of this BigInteger.  The byte array will be in
     * <i>big-endian</i> byte-order: the most significant byte is in
     * the zeroth element.  The array will contain the minimum number
     * of bytes required to represent this BigInteger, including at
     * least one sign bit, which is {@code (ceil((this.bitLength() +
     * 1)/8))}.  (This representation is compatible with the
     * {@link #BigInteger(byte[]) (byte[])} constructor.)
     *
     * @return a byte array containing the two's-complement representation of
     *         this BigInteger.
     * @see    #BigInteger(byte[])
     */
    public byte[] toByteArray() {
        int byteLen = bitLength()/8 + 1;
        byte[] byteArray = new byte[byteLen];

        for (int i=byteLen-1, bytesCopied=4, nextInt=0, intIndex=0; i >= 0; i--) {
            if (bytesCopied == 4) {
                nextInt = getInt(intIndex++);
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                bytesCopied++;
            }
            byteArray[i] = (byte)nextInt;
        }
        return byteArray;
    }

    /**
     * Converts this BigInteger to an {@code int}.  This
     * conversion is analogous to a
     * <i>narrowing primitive conversion</i> from {@code long} to
     * {@code int} as defined in section 5.1.3 of
     * <cite>The Java&trade; Language Specification</cite>:
     * if this BigInteger is too big to fit in an
     * {@code int}, only the low-order 32 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude of the BigInteger value as well as return a
     * result with the opposite sign.
     *
     * @return this BigInteger converted to an {@code int}.
     * @see #intValueExact()
     */
    public int intValue() {
        int result = 0;
        result = getInt(0);
        return result;
    }

    /**
     * Converts this BigInteger to a {@code long}.  This
     * conversion is analogous to a
     * <i>narrowing primitive conversion</i> from {@code long} to
     * {@code int} as defined in section 5.1.3 of
     * <cite>The Java&trade; Language Specification</cite>:
     * if this BigInteger is too big to fit in a
     * {@code long}, only the low-order 64 bits are returned.
     * Note that this conversion can lose information about the
     * overall magnitude of the BigInteger value as well as return a
     * result with the opposite sign.
     *
     * @return this BigInteger converted to a {@code long}.
     * @see #longValueExact()
     */
    public long longValue() {
        long result = 0;

        for (int i=1; i >= 0; i--)
            result = (result << 32) + (getInt(i) & LONG_MASK);
        return result;
    }

    /**
     * Converts this BigInteger to a {@code float}.  This
     * conversion is similar to the
     * <i>narrowing primitive conversion</i> from {@code double} to
     * {@code float} as defined in section 5.1.3 of
     * <cite>The Java&trade; Language Specification</cite>:
     * if this BigInteger has too great a magnitude
     * to represent as a {@code float}, it will be converted to
     * {@link Float#NEGATIVE_INFINITY} or {@link
     * Float#POSITIVE_INFINITY} as appropriate.  Note that even when
     * the return value is finite, this conversion can lose
     * information about the precision of the BigInteger value.
     *
     * @return this BigInteger converted to a {@code float}.
     */
    public float floatValue() {
        if (signum == 0) {
            return 0.0f;
        }

        int exponent = ((mag.length - 1) << 5) + bitLengthForInt(mag[0]) - 1;

        // exponent == floor(log2(abs(this)))
        if (exponent < Long.SIZE - 1) {
            return longValue();
        } else if (exponent > Float.MAX_EXPONENT) {
            return signum > 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        }

        /*
         * We need the top SIGNIFICAND_WIDTH bits, including the "implicit"
         * one bit. To make rounding easier, we pick out the top
         * SIGNIFICAND_WIDTH + 1 bits, so we have one to help us round up or
         * down. twiceSignifFloor will contain the top SIGNIFICAND_WIDTH + 1
         * bits, and signifFloor the top SIGNIFICAND_WIDTH.
         *
         * It helps to consider the real number signif = abs(this) *
         * 2^(SIGNIFICAND_WIDTH - 1 - exponent).
         */
        int shift = exponent - FloatConsts.SIGNIFICAND_WIDTH;

        int twiceSignifFloor;
        // twiceSignifFloor will be == abs().shiftRight(shift).intValue()
        // We do the shift into an int directly to improve performance.

        int nBits = shift & 0x1f;
        int nBits2 = 32 - nBits;

        if (nBits == 0) {
            twiceSignifFloor = mag[0];
        } else {
            twiceSignifFloor = mag[0] >>> nBits;
            if (twiceSignifFloor == 0) {
                twiceSignifFloor = (mag[0] << nBits2) | (mag[1] >>> nBits);
            }
        }

        int signifFloor = twiceSignifFloor >> 1;
        signifFloor &= FloatConsts.SIGNIF_BIT_MASK; // remove the implied bit

        /*
         * We round up if either the fractional part of signif is strictly
         * greater than 0.5 (which is true if the 0.5 bit is set and any lower
         * bit is set), or if the fractional part of signif is >= 0.5 and
         * signifFloor is odd (which is true if both the 0.5 bit and the 1 bit
         * are set). This is equivalent to the desired HALF_EVEN rounding.
         */
        boolean increment = (twiceSignifFloor & 1) != 0
                && ((signifFloor & 1) != 0 || abs().getLowestSetBit() < shift);
        int signifRounded = increment ? signifFloor + 1 : signifFloor;
        int bits = ((exponent + FloatConsts.EXP_BIAS))
                << (FloatConsts.SIGNIFICAND_WIDTH - 1);
        bits += signifRounded;
        /*
         * If signifRounded == 2^24, we'd need to set all of the significand
         * bits to zero and add 1 to the exponent. This is exactly the behavior
         * we get from just adding signifRounded to bits directly. If the
         * exponent is Float.MAX_EXPONENT, we round up (correctly) to
         * Float.POSITIVE_INFINITY.
         */
        bits |= signum & FloatConsts.SIGN_BIT_MASK;
        return Float.intBitsToFloat(bits);
    }

    /**
     * Converts this BigInteger to a {@code double}.  This
     * conversion is similar to the
     * <i>narrowing primitive conversion</i> from {@code double} to
     * {@code float} as defined in section 5.1.3 of
     * <cite>The Java&trade; Language Specification</cite>:
     * if this BigInteger has too great a magnitude
     * to represent as a {@code double}, it will be converted to
     * {@link Double#NEGATIVE_INFINITY} or {@link
     * Double#POSITIVE_INFINITY} as appropriate.  Note that even when
     * the return value is finite, this conversion can lose
     * information about the precision of the BigInteger value.
     *
     * @return this BigInteger converted to a {@code double}.
     */
    public double doubleValue() {
        if (signum == 0) {
            return 0.0;
        }

        int exponent = ((mag.length - 1) << 5) + bitLengthForInt(mag[0]) - 1;

        // exponent == floor(log2(abs(this))Double)
        if (exponent < Long.SIZE - 1) {
            return longValue();
        } else if (exponent > Double.MAX_EXPONENT) {
            return signum > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }

        /*
         * We need the top SIGNIFICAND_WIDTH bits, including the "implicit"
         * one bit. To make rounding easier, we pick out the top
         * SIGNIFICAND_WIDTH + 1 bits, so we have one to help us round up or
         * down. twiceSignifFloor will contain the top SIGNIFICAND_WIDTH + 1
         * bits, and signifFloor the top SIGNIFICAND_WIDTH.
         *
         * It helps to consider the real number signif = abs(this) *
         * 2^(SIGNIFICAND_WIDTH - 1 - exponent).
         */
        int shift = exponent - DoubleConsts.SIGNIFICAND_WIDTH;

        long twiceSignifFloor;
        // twiceSignifFloor will be == abs().shiftRight(shift).longValue()
        // We do the shift into a long directly to improve performance.

        int nBits = shift & 0x1f;
        int nBits2 = 32 - nBits;

        int highBits;
        int lowBits;
        if (nBits == 0) {
            highBits = mag[0];
            lowBits = mag[1];
        } else {
            highBits = mag[0] >>> nBits;
            lowBits = (mag[0] << nBits2) | (mag[1] >>> nBits);
            if (highBits == 0) {
                highBits = lowBits;
                lowBits = (mag[1] << nBits2) | (mag[2] >>> nBits);
            }
        }

        twiceSignifFloor = ((highBits & LONG_MASK) << 32)
                | (lowBits & LONG_MASK);

        long signifFloor = twiceSignifFloor >> 1;
        signifFloor &= DoubleConsts.SIGNIF_BIT_MASK; // remove the implied bit

        /*
         * We round up if either the fractional part of signif is strictly
         * greater than 0.5 (which is true if the 0.5 bit is set and any lower
         * bit is set), or if the fractional part of signif is >= 0.5 and
         * signifFloor is odd (which is true if both the 0.5 bit and the 1 bit
         * are set). This is equivalent to the desired HALF_EVEN rounding.
         */
        boolean increment = (twiceSignifFloor & 1) != 0
                && ((signifFloor & 1) != 0 || abs().getLowestSetBit() < shift);
        long signifRounded = increment ? signifFloor + 1 : signifFloor;
        long bits = (long) ((exponent + DoubleConsts.EXP_BIAS))
                << (DoubleConsts.SIGNIFICAND_WIDTH - 1);
        bits += signifRounded;
        /*
         * If signifRounded == 2^53, we'd need to set all of the significand
         * bits to zero and add 1 to the exponent. This is exactly the behavior
         * we get from just adding signifRounded to bits directly. If the
         * exponent is Double.MAX_EXPONENT, we round up (correctly) to
         * Double.POSITIVE_INFINITY.
         */
        bits |= signum & DoubleConsts.SIGN_BIT_MASK;
        return Double.longBitsToDouble(bits);
    }

    /**
     * Returns a copy of the input array stripped of any leading zero bytes.
     */
    private static int[] stripLeadingZeroInts(int val[]) {
        int vlen = val.length;
        int keep;

        // Find first nonzero byte
        for (keep = 0; keep < vlen && val[keep] == 0; keep++)
            ;
        return java.util.Arrays.copyOfRange(val, keep, vlen);
    }

    /**
     * Returns the input array stripped of any leading zero bytes.
     * Since the source is trusted the copying may be skipped.
     */
    private static int[] trustedStripLeadingZeroInts(int val[]) {
        int vlen = val.length;
        int keep;

        // Find first nonzero byte
        for (keep = 0; keep < vlen && val[keep] == 0; keep++)
            ;
        return keep == 0 ? val : java.util.Arrays.copyOfRange(val, keep, vlen);
    }

    /**
     * Returns a copy of the input array stripped of any leading zero bytes.
     */
    private static int[] stripLeadingZeroBytes(byte a[]) {
        int byteLength = a.length;
        int keep;

        // Find first nonzero byte
        for (keep = 0; keep < byteLength && a[keep] == 0; keep++)
            ;

        // Allocate new array and copy relevant part of input array
        int intLength = ((byteLength - keep) + 3) >>> 2;
        int[] result = new int[intLength];
        int b = byteLength - 1;
        for (int i = intLength-1; i >= 0; i--) {
            result[i] = a[b--] & 0xff;
            int bytesRemaining = b - keep + 1;
            int bytesToTransfer = Math.min(3, bytesRemaining);
            for (int j=8; j <= (bytesToTransfer << 3); j += 8)
                result[i] |= ((a[b--] & 0xff) << j);
        }
        return result;
    }

    /**
     * Takes an array a representing a negative 2's-complement number and
     * returns the minimal (no leading zero bytes) unsigned whose value is -a.
     */
    private static int[] makePositive(byte a[]) {
        int keep, k;
        int byteLength = a.length;

        // Find first non-sign (0xff) byte of input
        for (keep=0; keep < byteLength && a[keep] == -1; keep++)
            ;


        /* Allocate output array.  If all non-sign bytes are 0x00, we must
         * allocate space for one extra output byte. */
        for (k=keep; k < byteLength && a[k] == 0; k++)
            ;

        int extraByte = (k == byteLength) ? 1 : 0;
        int intLength = ((byteLength - keep + extraByte) + 3) >>> 2;
        int result[] = new int[intLength];

        /* Copy one's complement of input into output, leaving extra
         * byte (if it exists) == 0x00 */
        int b = byteLength - 1;
        for (int i = intLength-1; i >= 0; i--) {
            result[i] = a[b--] & 0xff;
            int numBytesToTransfer = Math.min(3, b-keep+1);
            if (numBytesToTransfer < 0)
                numBytesToTransfer = 0;
            for (int j=8; j <= 8*numBytesToTransfer; j += 8)
                result[i] |= ((a[b--] & 0xff) << j);

            // Mask indicates which bits must be complemented
            int mask = -1 >>> (8*(3-numBytesToTransfer));
            result[i] = ~result[i] & mask;
        }

        // Add one to one's complement to generate two's complement
        for (int i=result.length-1; i >= 0; i--) {
            result[i] = (int)((result[i] & LONG_MASK) + 1);
            if (result[i] != 0)
                break;
        }

        return result;
    }

    /**
     * Takes an array a representing a negative 2's-complement number and
     * returns the minimal (no leading zero ints) unsigned whose value is -a.
     */
    private static int[] makePositive(int a[]) {
        int keep, j;

        // Find first non-sign (0xffffffff) int of input
        for (keep=0; keep < a.length && a[keep] == -1; keep++)
            ;

        /* Allocate output array.  If all non-sign ints are 0x00, we must
         * allocate space for one extra output int. */
        for (j=keep; j < a.length && a[j] == 0; j++)
            ;
        int extraInt = (j == a.length ? 1 : 0);
        int result[] = new int[a.length - keep + extraInt];

        /* Copy one's complement of input into output, leaving extra
         * int (if it exists) == 0x00 */
        for (int i = keep; i < a.length; i++)
            result[i - keep + extraInt] = ~a[i];

        // Add one to one's complement to generate two's complement
        for (int i=result.length-1; ++result[i] == 0; i--)
            ;

        return result;
    }

    /*
     * The following two arrays are used for fast String conversions.  Both
     * are indexed by radix.  The first is the number of digits of the given
     * radix that can fit in a Java long without "going negative", i.e., the
     * highest integer n such that radix**n < 2**63.  The second is the
     * "long radix" that tears each number into "long digits", each of which
     * consists of the number of digits in the corresponding element in
     * digitsPerLong (longRadix[i] = i**digitPerLong[i]).  Both arrays have
     * nonsense values in their 0 and 1 elements, as radixes 0 and 1 are not
     * used.
     */
    private static int digitsPerLong[] = {0, 0,
        62, 39, 31, 27, 24, 22, 20, 19, 18, 18, 17, 17, 16, 16, 15, 15, 15, 14,
        14, 14, 14, 13, 13, 13, 13, 13, 13, 12, 12, 12, 12, 12, 12, 12, 12};

    private static BigInteger longRadix[] = {null, null,
        valueOf(0x4000000000000000L), valueOf(0x383d9170b85ff80bL),
        valueOf(0x4000000000000000L), valueOf(0x6765c793fa10079dL),
        valueOf(0x41c21cb8e1000000L), valueOf(0x3642798750226111L),
        valueOf(0x1000000000000000L), valueOf(0x12bf307ae81ffd59L),
        valueOf( 0xde0b6b3a7640000L), valueOf(0x4d28cb56c33fa539L),
        valueOf(0x1eca170c00000000L), valueOf(0x780c7372621bd74dL),
        valueOf(0x1e39a5057d810000L), valueOf(0x5b27ac993df97701L),
        valueOf(0x1000000000000000L), valueOf(0x27b95e997e21d9f1L),
        valueOf(0x5da0e1e53c5c8000L), valueOf( 0xb16a458ef403f19L),
        valueOf(0x16bcc41e90000000L), valueOf(0x2d04b7fdd9c0ef49L),
        valueOf(0x5658597bcaa24000L), valueOf( 0x6feb266931a75b7L),
        valueOf( 0xc29e98000000000L), valueOf(0x14adf4b7320334b9L),
        valueOf(0x226ed36478bfa000L), valueOf(0x383d9170b85ff80bL),
        valueOf(0x5a3c23e39c000000L), valueOf( 0x4e900abb53e6b71L),
        valueOf( 0x7600ec618141000L), valueOf( 0xaee5720ee830681L),
        valueOf(0x1000000000000000L), valueOf(0x172588ad4f5f0981L),
        valueOf(0x211e44f7d02c1000L), valueOf(0x2ee56725f06e5c71L),
        valueOf(0x41c21cb8e1000000L)};

    /*
     * These two arrays are the integer analogue of above.
     */
    private static int digitsPerInt[] = {0, 0, 30, 19, 15, 13, 11,
        11, 10, 9, 9, 8, 8, 8, 8, 7, 7, 7, 7, 7, 7, 7, 6, 6, 6, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 5};

    private static int intRadix[] = {0, 0,
        0x40000000, 0x4546b3db, 0x40000000, 0x48c27395, 0x159fd800,
        0x75db9c97, 0x40000000, 0x17179149, 0x3b9aca00, 0xcc6db61,
        0x19a10000, 0x309f1021, 0x57f6c100, 0xa2f1b6f,  0x10000000,
        0x18754571, 0x247dbc80, 0x3547667b, 0x4c4b4000, 0x6b5a6e1d,
        0x6c20a40,  0x8d2d931,  0xb640000,  0xe8d4a51,  0x1269ae40,
        0x17179149, 0x1cb91000, 0x23744899, 0x2b73a840, 0x34e63b41,
        0x40000000, 0x4cfa3cc1, 0x5c13d840, 0x6d91b519, 0x39aa400
    };

    /**
     * These routines provide access to the two's complement representation
     * of BigIntegers.
     */

    /**
     * Returns the length of the two's complement representation in ints,
     * including space for at least one sign bit.
     */
    private int intLength() {
        return (bitLength() >>> 5) + 1;
    }

    /* Returns sign bit */
    private int signBit() {
        return signum < 0 ? 1 : 0;
    }

    /* Returns an int of sign bits */
    private int signInt() {
        return signum < 0 ? -1 : 0;
    }

    /**
     * Returns the specified int of the little-endian two's complement
     * representation (int 0 is the least significant).  The int number can
     * be arbitrarily high (values are logically preceded by infinitely many
     * sign ints).
     */
    private int getInt(int n) {
        if (n < 0)
            return 0;
        if (n >= mag.length)
            return signInt();

        int magInt = mag[mag.length-n-1];

        return (signum >= 0 ? magInt :
                (n <= firstNonzeroIntNum() ? -magInt : ~magInt));
    }

    /**
     * Returns the index of the int that contains the first nonzero int in the
     * little-endian binary representation of the magnitude (int 0 is the
     * least significant). If the magnitude is zero, return value is undefined.
     */
    private int firstNonzeroIntNum() {
        int fn = firstNonzeroIntNum - 2;
        if (fn == -2) { // firstNonzeroIntNum not initialized yet
            fn = 0;

            // Search for the first nonzero int
            int i;
            int mlen = mag.length;
            for (i = mlen - 1; i >= 0 && mag[i] == 0; i--)
                ;
            fn = mlen - i - 1;
            firstNonzeroIntNum = fn + 2; // offset by two to initialize
        }
        return fn;
    }

    /** use serialVersionUID from JDK 1.1. for interoperability */
    private static final long serialVersionUID = -8287574255936472291L;

    /**
     * Serializable fields for BigInteger.
     *
     * @serialField signum  int
     *              signum of this BigInteger.
     * @serialField magnitude int[]
     *              magnitude array of this BigInteger.
     * @serialField bitCount  int
     *              number of bits in this BigInteger
     * @serialField bitLength int
     *              the number of bits in the minimal two's-complement
     *              representation of this BigInteger
     * @serialField lowestSetBit int
     *              lowest set bit in the twos complement representation
     */
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("signum", Integer.TYPE),
        new ObjectStreamField("magnitude", byte[].class),
        new ObjectStreamField("bitCount", Integer.TYPE),
        new ObjectStreamField("bitLength", Integer.TYPE),
        new ObjectStreamField("firstNonzeroByteNum", Integer.TYPE),
        new ObjectStreamField("lowestSetBit", Integer.TYPE)
        };

    /**
     * Reconstitute the {@code BigInteger} instance from a stream (that is,
     * deserialize it). The magnitude is read in as an array of bytes
     * for historical reasons, but it is converted to an array of ints
     * and the byte array is discarded.
     * Note:
     * The current convention is to initialize the cache fields, bitCount,
     * bitLength and lowestSetBit, to 0 rather than some other marker value.
     * Therefore, no explicit action to set these fields needs to be taken in
     * readObject because those fields already have a 0 value be default since
     * defaultReadObject is not being used.
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        /*
         * In order to maintain compatibility with previous serialized forms,
         * the magnitude of a BigInteger is serialized as an array of bytes.
         * The magnitude field is used as a temporary store for the byte array
         * that is deserialized. The cached computation fields should be
         * transient but are serialized for compatibility reasons.
         */

        // prepare to read the alternate persistent fields
        ObjectInputStream.GetField fields = s.readFields();

        // Read and validate the alternate persistent fields that we
        // care about, signum and magnitude

        // Read and validate signum
        int sign = fields.get("signum", -2);
        if (sign < -1 || sign > 1) {
            String message = "BigInteger: Invalid signum value";
            if (fields.defaulted("signum"))
                message = "BigInteger: Signum not present in stream";
            throw new java.io.StreamCorruptedException(message);
        }

        // Read and validate magnitude
        byte[] magnitude = (byte[])fields.get("magnitude", null);
        int[] mag = stripLeadingZeroBytes(magnitude);
        if ((mag.length == 0) != (sign == 0)) {
            String message = "BigInteger: signum-magnitude mismatch";
            if (fields.defaulted("magnitude"))
                message = "BigInteger: Magnitude not present in stream";
            throw new java.io.StreamCorruptedException(message);
        }

        // Equivalent to checkRange() on mag local without assigning
        // this.mag field
        if (mag.length > MAX_MAG_LENGTH ||
            (mag.length == MAX_MAG_LENGTH && mag[0] < 0)) {
            throw new java.io.StreamCorruptedException("BigInteger: Out of the supported range");
        }

        // Commit final fields via Unsafe
        UnsafeHolder.putSignAndMag(this, sign, mag);
    }

    /**
     * Serialization without data not supported for this class.
     */
    private void readObjectNoData()
        throws ObjectStreamException {
        throw new InvalidObjectException("Deserialized BigInteger objects need data");
    }

    // Support for resetting final fields while deserializing
    private static class UnsafeHolder {
        private static final sun.misc.Unsafe unsafe;
        private static final long signumOffset;
        private static final long magOffset;
        static {
            try {
                unsafe = sun.misc.Unsafe.getUnsafe();
                signumOffset = unsafe.objectFieldOffset
                    (BigInteger.class.getDeclaredField("signum"));
                magOffset = unsafe.objectFieldOffset
                    (BigInteger.class.getDeclaredField("mag"));
            } catch (Exception ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }

        static void putSignAndMag(BigInteger bi, int sign, int[] magnitude) {
            unsafe.putIntVolatile(bi, signumOffset, sign);
            unsafe.putObjectVolatile(bi, magOffset, magnitude);
        }
    }

    /**
     * Save the {@code BigInteger} instance to a stream.
     * The magnitude of a BigInteger is serialized as a byte array for
     * historical reasons.
     *
     * @serialData two necessary fields are written as well as obsolete
     *             fields for compatibility with older versions.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        // set the values of the Serializable fields
        ObjectOutputStream.PutField fields = s.putFields();
        fields.put("signum", signum);
        fields.put("magnitude", magSerializedForm());
        // The values written for cached fields are compatible with older
        // versions, but are ignored in readObject so don't otherwise matter.
        fields.put("bitCount", -1);
        fields.put("bitLength", -1);
        fields.put("lowestSetBit", -2);
        fields.put("firstNonzeroByteNum", -2);

        // save them
        s.writeFields();
}

    /**
     * Returns the mag array as an array of bytes.
     */
    private byte[] magSerializedForm() {
        int len = mag.length;

        int bitLen = (len == 0 ? 0 : ((len - 1) << 5) + bitLengthForInt(mag[0]));
        int byteLen = (bitLen + 7) >>> 3;
        byte[] result = new byte[byteLen];

        for (int i = byteLen - 1, bytesCopied = 4, intIndex = len - 1, nextInt = 0;
             i >= 0; i--) {
            if (bytesCopied == 4) {
                nextInt = mag[intIndex--];
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                bytesCopied++;
            }
            result[i] = (byte)nextInt;
        }
        return result;
    }

    /**
     * Converts this {@code BigInteger} to a {@code long}, checking
     * for lost information.  If the value of this {@code BigInteger}
     * is out of the range of the {@code long} type, then an
     * {@code ArithmeticException} is thrown.
     *
     * @return this {@code BigInteger} converted to a {@code long}.
     * @throws ArithmeticException if the value of {@code this} will
     * not exactly fit in a {@code long}.
     * @see BigInteger#longValue
     * @since  1.8
     */
    public long longValueExact() {
        if (mag.length <= 2 && bitLength() <= 63)
            return longValue();
        else
            throw new ArithmeticException("BigInteger out of long range");
    }

    /**
     * Converts this {@code BigInteger} to an {@code int}, checking
     * for lost information.  If the value of this {@code BigInteger}
     * is out of the range of the {@code int} type, then an
     * {@code ArithmeticException} is thrown.
     *
     * @return this {@code BigInteger} converted to an {@code int}.
     * @throws ArithmeticException if the value of {@code this} will
     * not exactly fit in a {@code int}.
     * @see BigInteger#intValue
     * @since  1.8
     */
    public int intValueExact() {
        if (mag.length <= 1 && bitLength() <= 31)
            return intValue();
        else
            throw new ArithmeticException("BigInteger out of int range");
    }

    /**
     * Converts this {@code BigInteger} to a {@code short}, checking
     * for lost information.  If the value of this {@code BigInteger}
     * is out of the range of the {@code short} type, then an
     * {@code ArithmeticException} is thrown.
     *
     * @return this {@code BigInteger} converted to a {@code short}.
     * @throws ArithmeticException if the value of {@code this} will
     * not exactly fit in a {@code short}.
     * @see BigInteger#shortValue
     * @since  1.8
     */
    public short shortValueExact() {
        if (mag.length <= 1 && bitLength() <= 31) {
            int value = intValue();
            if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
                return shortValue();
        }
        throw new ArithmeticException("BigInteger out of short range");
    }

    /**
     * Converts this {@code BigInteger} to a {@code byte}, checking
     * for lost information.  If the value of this {@code BigInteger}
     * is out of the range of the {@code byte} type, then an
     * {@code ArithmeticException} is thrown.
     *
     * @return this {@code BigInteger} converted to a {@code byte}.
     * @throws ArithmeticException if the value of {@code this} will
     * not exactly fit in a {@code byte}.
     * @see BigInteger#byteValue
     * @since  1.8
     */
    public byte byteValueExact() {
        if (mag.length <= 1 && bitLength() <= 31) {
            int value = intValue();
            if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
                return byteValue();
        }
        throw new ArithmeticException("BigInteger out of byte range");
    }
}
/*
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.math;

/**
 * A simple bit sieve used for finding prime number candidates. Allows setting
 * and clearing of bits in a storage array. The size of the sieve is assumed to
 * be constant to reduce overhead. All the bits of a new bitSieve are zero, and
 * bits are removed from it by setting them.
 *
 * To reduce storage space and increase efficiency, no even numbers are
 * represented in the sieve (each bit in the sieve represents an odd number).
 * The relationship between the index of a bit and the number it represents is
 * given by
 * N = offset + (2*index + 1);
 * Where N is the integer represented by a bit in the sieve, offset is some
 * even integer offset indicating where the sieve begins, and index is the
 * index of a bit in the sieve array.
 *
 * @see     BigInteger
 * @author  Michael McCloskey
 * @since   1.3
 */
class BitSieve {
    /**
     * Stores the bits in this bitSieve.
     */
    private long bits[];

    /**
     * Length is how many bits this sieve holds.
     */
    private int length;

    /**
     * A small sieve used to filter out multiples of small primes in a search
     * sieve.
     */
    private static BitSieve smallSieve = new BitSieve();

    /**
     * Construct a "small sieve" with a base of 0.  This constructor is
     * used internally to generate the set of "small primes" whose multiples
     * are excluded from sieves generated by the main (package private)
     * constructor, BitSieve(BigInteger base, int searchLen).  The length
     * of the sieve generated by this constructor was chosen for performance;
     * it controls a tradeoff between how much time is spent constructing
     * other sieves, and how much time is wasted testing composite candidates
     * for primality.  The length was chosen experimentally to yield good
     * performance.
     */
    private BitSieve() {
        length = 150 * 64;
        bits = new long[(unitIndex(length - 1) + 1)];

        // Mark 1 as composite
        set(0);
        int nextIndex = 1;
        int nextPrime = 3;

        // Find primes and remove their multiples from sieve
        do {
            sieveSingle(length, nextIndex + nextPrime, nextPrime);
            nextIndex = sieveSearch(length, nextIndex + 1);
            nextPrime = 2*nextIndex + 1;
        } while((nextIndex > 0) && (nextPrime < length));
    }

    /**
     * Construct a bit sieve of searchLen bits used for finding prime number
     * candidates. The new sieve begins at the specified base, which must
     * be even.
     */
    BitSieve(BigInteger base, int searchLen) {
        /*
         * Candidates are indicated by clear bits in the sieve. As a candidates
         * nonprimality is calculated, a bit is set in the sieve to eliminate
         * it. To reduce storage space and increase efficiency, no even numbers
         * are represented in the sieve (each bit in the sieve represents an
         * odd number).
         */
        bits = new long[(unitIndex(searchLen-1) + 1)];
        length = searchLen;
        int start = 0;

        int step = smallSieve.sieveSearch(smallSieve.length, start);
        int convertedStep = (step *2) + 1;

        // Construct the large sieve at an even offset specified by base
        MutableBigInteger b = new MutableBigInteger(base);
        MutableBigInteger q = new MutableBigInteger();
        do {
            // Calculate base mod convertedStep
            start = b.divideOneWord(convertedStep, q);

            // Take each multiple of step out of sieve
            start = convertedStep - start;
            if (start%2 == 0)
                start += convertedStep;
            sieveSingle(searchLen, (start-1)/2, convertedStep);

            // Find next prime from small sieve
            step = smallSieve.sieveSearch(smallSieve.length, step+1);
            convertedStep = (step *2) + 1;
        } while (step > 0);
    }

    /**
     * Given a bit index return unit index containing it.
     */
    private static int unitIndex(int bitIndex) {
        return bitIndex >>> 6;
    }

    /**
     * Return a unit that masks the specified bit in its unit.
     */
    private static long bit(int bitIndex) {
        return 1L << (bitIndex & ((1<<6) - 1));
    }

    /**
     * Get the value of the bit at the specified index.
     */
    private boolean get(int bitIndex) {
        int unitIndex = unitIndex(bitIndex);
        return ((bits[unitIndex] & bit(bitIndex)) != 0);
    }

    /**
     * Set the bit at the specified index.
     */
    private void set(int bitIndex) {
        int unitIndex = unitIndex(bitIndex);
        bits[unitIndex] |= bit(bitIndex);
    }

    /**
     * This method returns the index of the first clear bit in the search
     * array that occurs at or after start. It will not search past the
     * specified limit. It returns -1 if there is no such clear bit.
     */
    private int sieveSearch(int limit, int start) {
        if (start >= limit)
            return -1;

        int index = start;
        do {
            if (!get(index))
                return index;
            index++;
        } while(index < limit-1);
        return -1;
    }

    /**
     * Sieve a single set of multiples out of the sieve. Begin to remove
     * multiples of the specified step starting at the specified start index,
     * up to the specified limit.
     */
    private void sieveSingle(int limit, int start, int step) {
        while(start < limit) {
            set(start);
            start += step;
        }
    }

    /**
     * Test probable primes in the sieve and return successful candidates.
     */
    BigInteger retrieve(BigInteger initValue, int certainty, java.util.Random random) {
        // Examine the sieve one long at a time to find possible primes
        int offset = 1;
        for (int i=0; i<bits.length; i++) {
            long nextLong = ~bits[i];
            for (int j=0; j<64; j++) {
                if ((nextLong & 1) == 1) {
                    BigInteger candidate = initValue.add(
                                           BigInteger.valueOf(offset));
                    if (candidate.primeToCertainty(certainty, random))
                        return candidate;
                }
                nextLong >>>= 1;
                offset+=2;
            }
        }
        return null;
    }
}
/*
 * Copyright (c) 2003, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 * Portions Copyright IBM Corporation, 1997, 2001. All Rights Reserved.
 */

package java.math;
import java.io.*;

/**
 * Immutable objects which encapsulate the context settings which
 * describe certain rules for numerical operators, such as those
 * implemented by the {@link BigDecimal} class.
 *
 * <p>The base-independent settings are:
 * <ol>
 * <li>{@code precision}:
 * the number of digits to be used for an operation; results are
 * rounded to this precision
 *
 * <li>{@code roundingMode}:
 * a {@link RoundingMode} object which specifies the algorithm to be
 * used for rounding.
 * </ol>
 *
 * @see     BigDecimal
 * @see     RoundingMode
 * @author  Mike Cowlishaw
 * @author  Joseph D. Darcy
 * @since 1.5
 */

public final class MathContext implements Serializable {

    /* ----- Constants ----- */

    // defaults for constructors
    private static final int DEFAULT_DIGITS = 9;
    private static final RoundingMode DEFAULT_ROUNDINGMODE = RoundingMode.HALF_UP;
    // Smallest values for digits (Maximum is Integer.MAX_VALUE)
    private static final int MIN_DIGITS = 0;

    // Serialization version
    private static final long serialVersionUID = 5579720004786848255L;

    /* ----- Public Properties ----- */
    /**
     *  A {@code MathContext} object whose settings have the values
     *  required for unlimited precision arithmetic.
     *  The values of the settings are:
     *  <code>
     *  precision=0 roundingMode=HALF_UP
     *  </code>
     */
    public static final MathContext UNLIMITED =
        new MathContext(0, RoundingMode.HALF_UP);

    /**
     *  A {@code MathContext} object with a precision setting
     *  matching the IEEE 754R Decimal32 format, 7 digits, and a
     *  rounding mode of {@link RoundingMode#HALF_EVEN HALF_EVEN}, the
     *  IEEE 754R default.
     */
    public static final MathContext DECIMAL32 =
        new MathContext(7, RoundingMode.HALF_EVEN);

    /**
     *  A {@code MathContext} object with a precision setting
     *  matching the IEEE 754R Decimal64 format, 16 digits, and a
     *  rounding mode of {@link RoundingMode#HALF_EVEN HALF_EVEN}, the
     *  IEEE 754R default.
     */
    public static final MathContext DECIMAL64 =
        new MathContext(16, RoundingMode.HALF_EVEN);

    /**
     *  A {@code MathContext} object with a precision setting
     *  matching the IEEE 754R Decimal128 format, 34 digits, and a
     *  rounding mode of {@link RoundingMode#HALF_EVEN HALF_EVEN}, the
     *  IEEE 754R default.
     */
    public static final MathContext DECIMAL128 =
        new MathContext(34, RoundingMode.HALF_EVEN);

    /* ----- Shared Properties ----- */
    /**
     * The number of digits to be used for an operation.  A value of 0
     * indicates that unlimited precision (as many digits as are
     * required) will be used.  Note that leading zeros (in the
     * coefficient of a number) are never significant.
     *
     * <p>{@code precision} will always be non-negative.
     *
     * @serial
     */
    final int precision;

    /**
     * The rounding algorithm to be used for an operation.
     *
     * @see RoundingMode
     * @serial
     */
    final RoundingMode roundingMode;

    /* ----- Constructors ----- */

    /**
     * Constructs a new {@code MathContext} with the specified
     * precision and the {@link RoundingMode#HALF_UP HALF_UP} rounding
     * mode.
     *
     * @param setPrecision The non-negative {@code int} precision setting.
     * @throws IllegalArgumentException if the {@code setPrecision} parameter is less
     *         than zero.
     */
    public MathContext(int setPrecision) {
        this(setPrecision, DEFAULT_ROUNDINGMODE);
        return;
    }

    /**
     * Constructs a new {@code MathContext} with a specified
     * precision and rounding mode.
     *
     * @param setPrecision The non-negative {@code int} precision setting.
     * @param setRoundingMode The rounding mode to use.
     * @throws IllegalArgumentException if the {@code setPrecision} parameter is less
     *         than zero.
     * @throws NullPointerException if the rounding mode argument is {@code null}
     */
    public MathContext(int setPrecision,
                       RoundingMode setRoundingMode) {
        if (setPrecision < MIN_DIGITS)
            throw new IllegalArgumentException("Digits < 0");
        if (setRoundingMode == null)
            throw new NullPointerException("null RoundingMode");

        precision = setPrecision;
        roundingMode = setRoundingMode;
        return;
    }

    /**
     * Constructs a new {@code MathContext} from a string.
     *
     * The string must be in the same format as that produced by the
     * {@link #toString} method.
     *
     * <p>An {@code IllegalArgumentException} is thrown if the precision
     * section of the string is out of range ({@code < 0}) or the string is
     * not in the format created by the {@link #toString} method.
     *
     * @param val The string to be parsed
     * @throws IllegalArgumentException if the precision section is out of range
     * or of incorrect format
     * @throws NullPointerException if the argument is {@code null}
     */
    public MathContext(String val) {
        boolean bad = false;
        int setPrecision;
        if (val == null)
            throw new NullPointerException("null String");
        try { // any error here is a string format problem
            if (!val.startsWith("precision=")) throw new RuntimeException();
            int fence = val.indexOf(' ');    // could be -1
            int off = 10;                     // where value starts
            setPrecision = Integer.parseInt(val.substring(10, fence));

            if (!val.startsWith("roundingMode=", fence+1))
                throw new RuntimeException();
            off = fence + 1 + 13;
            String str = val.substring(off, val.length());
            roundingMode = RoundingMode.valueOf(str);
        } catch (RuntimeException re) {
            throw new IllegalArgumentException("bad string format");
        }

        if (setPrecision < MIN_DIGITS)
            throw new IllegalArgumentException("Digits < 0");
        // the other parameters cannot be invalid if we got here
        precision = setPrecision;
    }

    /**
     * Returns the {@code precision} setting.
     * This value is always non-negative.
     *
     * @return an {@code int} which is the value of the {@code precision}
     *         setting
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * Returns the roundingMode setting.
     * This will be one of
     * {@link  RoundingMode#CEILING},
     * {@link  RoundingMode#DOWN},
     * {@link  RoundingMode#FLOOR},
     * {@link  RoundingMode#HALF_DOWN},
     * {@link  RoundingMode#HALF_EVEN},
     * {@link  RoundingMode#HALF_UP},
     * {@link  RoundingMode#UNNECESSARY}, or
     * {@link  RoundingMode#UP}.
     *
     * @return a {@code RoundingMode} object which is the value of the
     *         {@code roundingMode} setting
     */

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    /**
     * Compares this {@code MathContext} with the specified
     * {@code Object} for equality.
     *
     * @param  x {@code Object} to which this {@code MathContext} is to
     *         be compared.
     * @return {@code true} if and only if the specified {@code Object} is
     *         a {@code MathContext} object which has exactly the same
     *         settings as this object
     */
    public boolean equals(Object x){
        MathContext mc;
        if (!(x instanceof MathContext))
            return false;
        mc = (MathContext) x;
        return mc.precision == this.precision
            && mc.roundingMode == this.roundingMode; // no need for .equals()
    }

    /**
     * Returns the hash code for this {@code MathContext}.
     *
     * @return hash code for this {@code MathContext}
     */
    public int hashCode() {
        return this.precision + roundingMode.hashCode() * 59;
    }

    /**
     * Returns the string representation of this {@code MathContext}.
     * The {@code String} returned represents the settings of the
     * {@code MathContext} object as two space-delimited words
     * (separated by a single space character, <tt>'&#92;u0020'</tt>,
     * and with no leading or trailing white space), as follows:
     * <ol>
     * <li>
     * The string {@code "precision="}, immediately followed
     * by the value of the precision setting as a numeric string as if
     * generated by the {@link Integer#toString(int) Integer.toString}
     * method.
     *
     * <li>
     * The string {@code "roundingMode="}, immediately
     * followed by the value of the {@code roundingMode} setting as a
     * word.  This word will be the same as the name of the
     * corresponding public constant in the {@link RoundingMode}
     * enum.
     * </ol>
     * <p>
     * For example:
     * <pre>
     * precision=9 roundingMode=HALF_UP
     * </pre>
     *
     * Additional words may be appended to the result of
     * {@code toString} in the future if more properties are added to
     * this class.
     *
     * @return a {@code String} representing the context settings
     */
    public java.lang.String toString() {
        return "precision=" +           precision + " " +
               "roundingMode=" +        roundingMode.toString();
    }

    // Private methods

    /**
     * Reconstitute the {@code MathContext} instance from a stream (that is,
     * deserialize it).
     *
     * @param s the stream being read.
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();     // read in all fields
        // validate possibly bad fields
        if (precision < MIN_DIGITS) {
            String message = "MathContext: invalid digits in stream";
            throw new java.io.StreamCorruptedException(message);
        }
        if (roundingMode == null) {
            String message = "MathContext: null roundingMode in stream";
            throw new java.io.StreamCorruptedException(message);
        }
    }

}
/*
 * Copyright (c) 1999, 2020, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.math;

/**
 * A class used to represent multiprecision integers that makes efficient
 * use of allocated space by allowing a number to occupy only part of
 * an array so that the arrays do not have to be reallocated as often.
 * When performing an operation with many iterations the array used to
 * hold a number is only reallocated when necessary and does not have to
 * be the same size as the number it represents. A mutable number allows
 * calculations to occur on the same number without having to create
 * a new number for every step of the calculation as occurs with
 * BigIntegers.
 *
 * @see     BigInteger
 * @author  Michael McCloskey
 * @author  Timothy Buktu
 * @since   1.3
 */

import static java.math.BigDecimal.INFLATED;
import static java.math.BigInteger.LONG_MASK;
import java.util.Arrays;

class MutableBigInteger {
    /**
     * Holds the magnitude of this MutableBigInteger in big endian order.
     * The magnitude may start at an offset into the value array, and it may
     * end before the length of the value array.
     */
    int[] value;

    /**
     * The number of ints of the value array that are currently used
     * to hold the magnitude of this MutableBigInteger. The magnitude starts
     * at an offset and offset + intLen may be less than value.length.
     */
    int intLen;

    /**
     * The offset into the value array where the magnitude of this
     * MutableBigInteger begins.
     */
    int offset = 0;

    // Constants
    /**
     * MutableBigInteger with one element value array with the value 1. Used by
     * BigDecimal divideAndRound to increment the quotient. Use this constant
     * only when the method is not going to modify this object.
     */
    static final MutableBigInteger ONE = new MutableBigInteger(1);

    /**
     * The minimum {@code intLen} for cancelling powers of two before
     * dividing.
     * If the number of ints is less than this threshold,
     * {@code divideKnuth} does not eliminate common powers of two from
     * the dividend and divisor.
     */
    static final int KNUTH_POW2_THRESH_LEN = 6;

    /**
     * The minimum number of trailing zero ints for cancelling powers of two
     * before dividing.
     * If the dividend and divisor don't share at least this many zero ints
     * at the end, {@code divideKnuth} does not eliminate common powers
     * of two from the dividend and divisor.
     */
    static final int KNUTH_POW2_THRESH_ZEROS = 3;

    // Constructors

    /**
     * The default constructor. An empty MutableBigInteger is created with
     * a one word capacity.
     */
    MutableBigInteger() {
        value = new int[1];
        intLen = 0;
    }

    /**
     * Construct a new MutableBigInteger with a magnitude specified by
     * the int val.
     */
    MutableBigInteger(int val) {
        value = new int[1];
        intLen = 1;
        value[0] = val;
    }

    /**
     * Construct a new MutableBigInteger with the specified value array
     * up to the length of the array supplied.
     */
    MutableBigInteger(int[] val) {
        value = val;
        intLen = val.length;
    }

    /**
     * Construct a new MutableBigInteger with a magnitude equal to the
     * specified BigInteger.
     */
    MutableBigInteger(BigInteger b) {
        intLen = b.mag.length;
        value = Arrays.copyOf(b.mag, intLen);
    }

    /**
     * Construct a new MutableBigInteger with a magnitude equal to the
     * specified MutableBigInteger.
     */
    MutableBigInteger(MutableBigInteger val) {
        intLen = val.intLen;
        value = Arrays.copyOfRange(val.value, val.offset, val.offset + intLen);
    }

    /**
     * Makes this number an {@code n}-int number all of whose bits are ones.
     * Used by Burnikel-Ziegler division.
     * @param n number of ints in the {@code value} array
     * @return a number equal to {@code ((1<<(32*n)))-1}
     */
    private void ones(int n) {
        if (n > value.length)
            value = new int[n];
        Arrays.fill(value, -1);
        offset = 0;
        intLen = n;
    }

    /**
     * Internal helper method to return the magnitude array. The caller is not
     * supposed to modify the returned array.
     */
    private int[] getMagnitudeArray() {
        if (offset > 0 || value.length != intLen)
            return Arrays.copyOfRange(value, offset, offset + intLen);
        return value;
    }

    /**
     * Convert this MutableBigInteger to a long value. The caller has to make
     * sure this MutableBigInteger can be fit into long.
     */
    private long toLong() {
        assert (intLen <= 2) : "this MutableBigInteger exceeds the range of long";
        if (intLen == 0)
            return 0;
        long d = value[offset] & LONG_MASK;
        return (intLen == 2) ? d << 32 | (value[offset + 1] & LONG_MASK) : d;
    }

    /**
     * Convert this MutableBigInteger to a BigInteger object.
     */
    BigInteger toBigInteger(int sign) {
        if (intLen == 0 || sign == 0)
            return BigInteger.ZERO;
        return new BigInteger(getMagnitudeArray(), sign);
    }

    /**
     * Converts this number to a nonnegative {@code BigInteger}.
     */
    BigInteger toBigInteger() {
        normalize();
        return toBigInteger(isZero() ? 0 : 1);
    }

    /**
     * Convert this MutableBigInteger to BigDecimal object with the specified sign
     * and scale.
     */
    BigDecimal toBigDecimal(int sign, int scale) {
        if (intLen == 0 || sign == 0)
            return BigDecimal.zeroValueOf(scale);
        int[] mag = getMagnitudeArray();
        int len = mag.length;
        int d = mag[0];
        // If this MutableBigInteger can't be fit into long, we need to
        // make a BigInteger object for the resultant BigDecimal object.
        if (len > 2 || (d < 0 && len == 2))
            return new BigDecimal(new BigInteger(mag, sign), INFLATED, scale, 0);
        long v = (len == 2) ?
            ((mag[1] & LONG_MASK) | (d & LONG_MASK) << 32) :
            d & LONG_MASK;
        return BigDecimal.valueOf(sign == -1 ? -v : v, scale);
    }

    /**
     * This is for internal use in converting from a MutableBigInteger
     * object into a long value given a specified sign.
     * returns INFLATED if value is not fit into long
     */
    long toCompactValue(int sign) {
        if (intLen == 0 || sign == 0)
            return 0L;
        int[] mag = getMagnitudeArray();
        int len = mag.length;
        int d = mag[0];
        // If this MutableBigInteger can not be fitted into long, we need to
        // make a BigInteger object for the resultant BigDecimal object.
        if (len > 2 || (d < 0 && len == 2))
            return INFLATED;
        long v = (len == 2) ?
            ((mag[1] & LONG_MASK) | (d & LONG_MASK) << 32) :
            d & LONG_MASK;
        return sign == -1 ? -v : v;
    }

    /**
     * Clear out a MutableBigInteger for reuse.
     */
    void clear() {
        offset = intLen = 0;
        for (int index=0, n=value.length; index < n; index++)
            value[index] = 0;
    }

    /**
     * Set a MutableBigInteger to zero, removing its offset.
     */
    void reset() {
        offset = intLen = 0;
    }

    /**
     * Compare the magnitude of two MutableBigIntegers. Returns -1, 0 or 1
     * as this MutableBigInteger is numerically less than, equal to, or
     * greater than <tt>b</tt>.
     */
    final int compare(MutableBigInteger b) {
        int blen = b.intLen;
        if (intLen < blen)
            return -1;
        if (intLen > blen)
           return 1;

        // Add Integer.MIN_VALUE to make the comparison act as unsigned integer
        // comparison.
        int[] bval = b.value;
        for (int i = offset, j = b.offset; i < intLen + offset; i++, j++) {
            int b1 = value[i] + 0x80000000;
            int b2 = bval[j]  + 0x80000000;
            if (b1 < b2)
                return -1;
            if (b1 > b2)
                return 1;
        }
        return 0;
    }

    /**
     * Returns a value equal to what {@code b.leftShift(32*ints); return compare(b);}
     * would return, but doesn't change the value of {@code b}.
     */
    private int compareShifted(MutableBigInteger b, int ints) {
        int blen = b.intLen;
        int alen = intLen - ints;
        if (alen < blen)
            return -1;
        if (alen > blen)
           return 1;

        // Add Integer.MIN_VALUE to make the comparison act as unsigned integer
        // comparison.
        int[] bval = b.value;
        for (int i = offset, j = b.offset; i < alen + offset; i++, j++) {
            int b1 = value[i] + 0x80000000;
            int b2 = bval[j]  + 0x80000000;
            if (b1 < b2)
                return -1;
            if (b1 > b2)
                return 1;
        }
        return 0;
    }

    /**
     * Compare this against half of a MutableBigInteger object (Needed for
     * remainder tests).
     * Assumes no leading unnecessary zeros, which holds for results
     * from divide().
     */
    final int compareHalf(MutableBigInteger b) {
        int blen = b.intLen;
        int len = intLen;
        if (len <= 0)
            return blen <= 0 ? 0 : -1;
        if (len > blen)
            return 1;
        if (len < blen - 1)
            return -1;
        int[] bval = b.value;
        int bstart = 0;
        int carry = 0;
        // Only 2 cases left:len == blen or len == blen - 1
        if (len != blen) { // len == blen - 1
            if (bval[bstart] == 1) {
                ++bstart;
                carry = 0x80000000;
            } else
                return -1;
        }
        // compare values with right-shifted values of b,
        // carrying shifted-out bits across words
        int[] val = value;
        for (int i = offset, j = bstart; i < len + offset;) {
            int bv = bval[j++];
            long hb = ((bv >>> 1) + carry) & LONG_MASK;
            long v = val[i++] & LONG_MASK;
            if (v != hb)
                return v < hb ? -1 : 1;
            carry = (bv & 1) << 31; // carray will be either 0x80000000 or 0
        }
        return carry == 0 ? 0 : -1;
    }

    /**
     * Return the index of the lowest set bit in this MutableBigInteger. If the
     * magnitude of this MutableBigInteger is zero, -1 is returned.
     */
    private final int getLowestSetBit() {
        if (intLen == 0)
            return -1;
        int j, b;
        for (j=intLen-1; (j > 0) && (value[j+offset] == 0); j--)
            ;
        b = value[j+offset];
        if (b == 0)
            return -1;
        return ((intLen-1-j)<<5) + Integer.numberOfTrailingZeros(b);
    }

    /**
     * Return the int in use in this MutableBigInteger at the specified
     * index. This method is not used because it is not inlined on all
     * platforms.
     */
    private final int getInt(int index) {
        return value[offset+index];
    }

    /**
     * Return a long which is equal to the unsigned value of the int in
     * use in this MutableBigInteger at the specified index. This method is
     * not used because it is not inlined on all platforms.
     */
    private final long getLong(int index) {
        return value[offset+index] & LONG_MASK;
    }

    /**
     * Ensure that the MutableBigInteger is in normal form, specifically
     * making sure that there are no leading zeros, and that if the
     * magnitude is zero, then intLen is zero.
     */
    final void normalize() {
        if (intLen == 0) {
            offset = 0;
            return;
        }

        int index = offset;
        if (value[index] != 0)
            return;

        int indexBound = index+intLen;
        do {
            index++;
        } while(index < indexBound && value[index] == 0);

        int numZeros = index - offset;
        intLen -= numZeros;
        offset = (intLen == 0 ?  0 : offset+numZeros);
    }

    /**
     * If this MutableBigInteger cannot hold len words, increase the size
     * of the value array to len words.
     */
    private final void ensureCapacity(int len) {
        if (value.length < len) {
            value = new int[len];
            offset = 0;
            intLen = len;
        }
    }

    /**
     * Convert this MutableBigInteger into an int array with no leading
     * zeros, of a length that is equal to this MutableBigInteger's intLen.
     */
    int[] toIntArray() {
        int[] result = new int[intLen];
        for(int i=0; i < intLen; i++)
            result[i] = value[offset+i];
        return result;
    }

    /**
     * Sets the int at index+offset in this MutableBigInteger to val.
     * This does not get inlined on all platforms so it is not used
     * as often as originally intended.
     */
    void setInt(int index, int val) {
        value[offset + index] = val;
    }

    /**
     * Sets this MutableBigInteger's value array to the specified array.
     * The intLen is set to the specified length.
     */
    void setValue(int[] val, int length) {
        value = val;
        intLen = length;
        offset = 0;
    }

    /**
     * Sets this MutableBigInteger's value array to a copy of the specified
     * array. The intLen is set to the length of the new array.
     */
    void copyValue(MutableBigInteger src) {
        int len = src.intLen;
        if (value.length < len)
            value = new int[len];
        System.arraycopy(src.value, src.offset, value, 0, len);
        intLen = len;
        offset = 0;
    }

    /**
     * Sets this MutableBigInteger's value array to a copy of the specified
     * array. The intLen is set to the length of the specified array.
     */
    void copyValue(int[] val) {
        int len = val.length;
        if (value.length < len)
            value = new int[len];
        System.arraycopy(val, 0, value, 0, len);
        intLen = len;
        offset = 0;
    }

    /**
     * Returns true iff this MutableBigInteger has a value of one.
     */
    boolean isOne() {
        return (intLen == 1) && (value[offset] == 1);
    }

    /**
     * Returns true iff this MutableBigInteger has a value of zero.
     */
    boolean isZero() {
        return (intLen == 0);
    }

    /**
     * Returns true iff this MutableBigInteger is even.
     */
    boolean isEven() {
        return (intLen == 0) || ((value[offset + intLen - 1] & 1) == 0);
    }

    /**
     * Returns true iff this MutableBigInteger is odd.
     */
    boolean isOdd() {
        return isZero() ? false : ((value[offset + intLen - 1] & 1) == 1);
    }

    /**
     * Returns true iff this MutableBigInteger is in normal form. A
     * MutableBigInteger is in normal form if it has no leading zeros
     * after the offset, and intLen + offset <= value.length.
     */
    boolean isNormal() {
        if (intLen + offset > value.length)
            return false;
        if (intLen == 0)
            return true;
        return (value[offset] != 0);
    }

    /**
     * Returns a String representation of this MutableBigInteger in radix 10.
     */
    public String toString() {
        BigInteger b = toBigInteger(1);
        return b.toString();
    }

    /**
     * Like {@link #rightShift(int)} but {@code n} can be greater than the length of the number.
     */
    void safeRightShift(int n) {
        if (n/32 >= intLen) {
            reset();
        } else {
            rightShift(n);
        }
    }

    /**
     * Right shift this MutableBigInteger n bits. The MutableBigInteger is left
     * in normal form.
     */
    void rightShift(int n) {
        if (intLen == 0)
            return;
        int nInts = n >>> 5;
        int nBits = n & 0x1F;
        this.intLen -= nInts;
        if (nBits == 0)
            return;
        int bitsInHighWord = BigInteger.bitLengthForInt(value[offset]);
        if (nBits >= bitsInHighWord) {
            this.primitiveLeftShift(32 - nBits);
            this.intLen--;
        } else {
            primitiveRightShift(nBits);
        }
    }

    /**
     * Like {@link #leftShift(int)} but {@code n} can be zero.
     */
    void safeLeftShift(int n) {
        if (n > 0) {
            leftShift(n);
        }
    }

    /**
     * Left shift this MutableBigInteger n bits.
     */
    void leftShift(int n) {
        /*
         * If there is enough storage space in this MutableBigInteger already
         * the available space will be used. Space to the right of the used
         * ints in the value array is faster to utilize, so the extra space
         * will be taken from the right if possible.
         */
        if (intLen == 0)
           return;
        int nInts = n >>> 5;
        int nBits = n&0x1F;
        int bitsInHighWord = BigInteger.bitLengthForInt(value[offset]);

        // If shift can be done without moving words, do so
        if (n <= (32-bitsInHighWord)) {
            primitiveLeftShift(nBits);
            return;
        }

        int newLen = intLen + nInts +1;
        if (nBits <= (32-bitsInHighWord))
            newLen--;
        if (value.length < newLen) {
            // The array must grow
            int[] result = new int[newLen];
            for (int i=0; i < intLen; i++)
                result[i] = value[offset+i];
            setValue(result, newLen);
        } else if (value.length - offset >= newLen) {
            // Use space on right
            for(int i=0; i < newLen - intLen; i++)
                value[offset+intLen+i] = 0;
        } else {
            // Must use space on left
            for (int i=0; i < intLen; i++)
                value[i] = value[offset+i];
            for (int i=intLen; i < newLen; i++)
                value[i] = 0;
            offset = 0;
        }
        intLen = newLen;
        if (nBits == 0)
            return;
        if (nBits <= (32-bitsInHighWord))
            primitiveLeftShift(nBits);
        else
            primitiveRightShift(32 -nBits);
    }

    /**
     * A primitive used for division. This method adds in one multiple of the
     * divisor a back to the dividend result at a specified offset. It is used
     * when qhat was estimated too large, and must be adjusted.
     */
    private int divadd(int[] a, int[] result, int offset) {
        long carry = 0;

        for (int j=a.length-1; j >= 0; j--) {
            long sum = (a[j] & LONG_MASK) +
                       (result[j+offset] & LONG_MASK) + carry;
            result[j+offset] = (int)sum;
            carry = sum >>> 32;
        }
        return (int)carry;
    }

    /**
     * This method is used for division. It multiplies an n word input a by one
     * word input x, and subtracts the n word product from q. This is needed
     * when subtracting qhat*divisor from dividend.
     */
    private int mulsub(int[] q, int[] a, int x, int len, int offset) {
        long xLong = x & LONG_MASK;
        long carry = 0;
        offset += len;

        for (int j=len-1; j >= 0; j--) {
            long product = (a[j] & LONG_MASK) * xLong + carry;
            long difference = q[offset] - product;
            q[offset--] = (int)difference;
            carry = (product >>> 32)
                     + (((difference & LONG_MASK) >
                         (((~(int)product) & LONG_MASK))) ? 1:0);
        }
        return (int)carry;
    }

    /**
     * The method is the same as mulsun, except the fact that q array is not
     * updated, the only result of the method is borrow flag.
     */
    private int mulsubBorrow(int[] q, int[] a, int x, int len, int offset) {
        long xLong = x & LONG_MASK;
        long carry = 0;
        offset += len;
        for (int j=len-1; j >= 0; j--) {
            long product = (a[j] & LONG_MASK) * xLong + carry;
            long difference = q[offset--] - product;
            carry = (product >>> 32)
                     + (((difference & LONG_MASK) >
                         (((~(int)product) & LONG_MASK))) ? 1:0);
        }
        return (int)carry;
    }

    /**
     * Right shift this MutableBigInteger n bits, where n is
     * less than 32.
     * Assumes that intLen > 0, n > 0 for speed
     */
    private final void primitiveRightShift(int n) {
        int[] val = value;
        int n2 = 32 - n;
        for (int i=offset+intLen-1, c=val[i]; i > offset; i--) {
            int b = c;
            c = val[i-1];
            val[i] = (c << n2) | (b >>> n);
        }
        val[offset] >>>= n;
    }

    /**
     * Left shift this MutableBigInteger n bits, where n is
     * less than 32.
     * Assumes that intLen > 0, n > 0 for speed
     */
    private final void primitiveLeftShift(int n) {
        int[] val = value;
        int n2 = 32 - n;
        for (int i=offset, c=val[i], m=i+intLen-1; i < m; i++) {
            int b = c;
            c = val[i+1];
            val[i] = (b << n) | (c >>> n2);
        }
        val[offset+intLen-1] <<= n;
    }

    /**
     * Returns a {@code BigInteger} equal to the {@code n}
     * low ints of this number.
     */
    private BigInteger getLower(int n) {
        if (isZero()) {
            return BigInteger.ZERO;
        } else if (intLen < n) {
            return toBigInteger(1);
        } else {
            // strip zeros
            int len = n;
            while (len > 0 && value[offset+intLen-len] == 0)
                len--;
            int sign = len > 0 ? 1 : 0;
            return new BigInteger(Arrays.copyOfRange(value, offset+intLen-len, offset+intLen), sign);
        }
    }

    /**
     * Discards all ints whose index is greater than {@code n}.
     */
    private void keepLower(int n) {
        if (intLen >= n) {
            offset += intLen - n;
            intLen = n;
        }
    }

    /**
     * Adds the contents of two MutableBigInteger objects.The result
     * is placed within this MutableBigInteger.
     * The contents of the addend are not changed.
     */
    void add(MutableBigInteger addend) {
        int x = intLen;
        int y = addend.intLen;
        int resultLen = (intLen > addend.intLen ? intLen : addend.intLen);
        int[] result = (value.length < resultLen ? new int[resultLen] : value);

        int rstart = result.length-1;
        long sum;
        long carry = 0;

        // Add common parts of both numbers
        while(x > 0 && y > 0) {
            x--; y--;
            sum = (value[x+offset] & LONG_MASK) +
                (addend.value[y+addend.offset] & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }

        // Add remainder of the longer number
        while(x > 0) {
            x--;
            if (carry == 0 && result == value && rstart == (x + offset))
                return;
            sum = (value[x+offset] & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }
        while(y > 0) {
            y--;
            sum = (addend.value[y+addend.offset] & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }

        if (carry > 0) { // Result must grow in length
            resultLen++;
            if (result.length < resultLen) {
                int temp[] = new int[resultLen];
                // Result one word longer from carry-out; copy low-order
                // bits into new result.
                System.arraycopy(result, 0, temp, 1, result.length);
                temp[0] = 1;
                result = temp;
            } else {
                result[rstart--] = 1;
            }
        }

        value = result;
        intLen = resultLen;
        offset = result.length - resultLen;
    }

    /**
     * Adds the value of {@code addend} shifted {@code n} ints to the left.
     * Has the same effect as {@code addend.leftShift(32*ints); add(addend);}
     * but doesn't change the value of {@code addend}.
     */
    void addShifted(MutableBigInteger addend, int n) {
        if (addend.isZero()) {
            return;
        }

        int x = intLen;
        int y = addend.intLen + n;
        int resultLen = (intLen > y ? intLen : y);
        int[] result = (value.length < resultLen ? new int[resultLen] : value);

        int rstart = result.length-1;
        long sum;
        long carry = 0;

        // Add common parts of both numbers
        while (x > 0 && y > 0) {
            x--; y--;
            int bval = y+addend.offset < addend.value.length ? addend.value[y+addend.offset] : 0;
            sum = (value[x+offset] & LONG_MASK) +
                (bval & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }

        // Add remainder of the longer number
        while (x > 0) {
            x--;
            if (carry == 0 && result == value && rstart == (x + offset)) {
                return;
            }
            sum = (value[x+offset] & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }
        while (y > 0) {
            y--;
            int bval = y+addend.offset < addend.value.length ? addend.value[y+addend.offset] : 0;
            sum = (bval & LONG_MASK) + carry;
            result[rstart--] = (int)sum;
            carry = sum >>> 32;
        }

        if (carry > 0) { // Result must grow in length
            resultLen++;
            if (result.length < resultLen) {
                int temp[] = new int[resultLen];
                // Result one word longer from carry-out; copy low-order
                // bits into new result.
                System.arraycopy(result, 0, temp, 1, result.length);
                temp[0] = 1;
                result = temp;
            } else {
                result[rstart--] = 1;
            }
        }

        value = result;
        intLen = resultLen;
        offset = result.length - resultLen;
    }

    /**
     * Like {@link #addShifted(MutableBigInteger, int)} but {@code this.intLen} must
     * not be greater than {@code n}. In other words, concatenates {@code this}
     * and {@code addend}.
     */
    void addDisjoint(MutableBigInteger addend, int n) {
        if (addend.isZero())
            return;

        int x = intLen;
        int y = addend.intLen + n;
        int resultLen = (intLen > y ? intLen : y);
        int[] result;
        if (value.length < resultLen)
            result = new int[resultLen];
        else {
            result = value;
            Arrays.fill(value, offset+intLen, value.length, 0);
        }

        int rstart = result.length-1;

        // copy from this if needed
        System.arraycopy(value, offset, result, rstart+1-x, x);
        y -= x;
        rstart -= x;

        int len = Math.min(y, addend.value.length-addend.offset);
        System.arraycopy(addend.value, addend.offset, result, rstart+1-y, len);

        // zero the gap
        for (int i=rstart+1-y+len; i < rstart+1; i++)
            result[i] = 0;

        value = result;
        intLen = resultLen;
        offset = result.length - resultLen;
    }

    /**
     * Adds the low {@code n} ints of {@code addend}.
     */
    void addLower(MutableBigInteger addend, int n) {
        MutableBigInteger a = new MutableBigInteger(addend);
        if (a.offset + a.intLen >= n) {
            a.offset = a.offset + a.intLen - n;
            a.intLen = n;
        }
        a.normalize();
        add(a);
    }

    /**
     * Subtracts the smaller of this and b from the larger and places the
     * result into this MutableBigInteger.
     */
    int subtract(MutableBigInteger b) {
        MutableBigInteger a = this;

        int[] result = value;
        int sign = a.compare(b);

        if (sign == 0) {
            reset();
            return 0;
        }
        if (sign < 0) {
            MutableBigInteger tmp = a;
            a = b;
            b = tmp;
        }

        int resultLen = a.intLen;
        if (result.length < resultLen)
            result = new int[resultLen];

        long diff = 0;
        int x = a.intLen;
        int y = b.intLen;
        int rstart = result.length - 1;

        // Subtract common parts of both numbers
        while (y > 0) {
            x--; y--;

            diff = (a.value[x+a.offset] & LONG_MASK) -
                   (b.value[y+b.offset] & LONG_MASK) - ((int)-(diff>>32));
            result[rstart--] = (int)diff;
        }
        // Subtract remainder of longer number
        while (x > 0) {
            x--;
            diff = (a.value[x+a.offset] & LONG_MASK) - ((int)-(diff>>32));
            result[rstart--] = (int)diff;
        }

        value = result;
        intLen = resultLen;
        offset = value.length - resultLen;
        normalize();
        return sign;
    }

    /**
     * Subtracts the smaller of a and b from the larger and places the result
     * into the larger. Returns 1 if the answer is in a, -1 if in b, 0 if no
     * operation was performed.
     */
    private int difference(MutableBigInteger b) {
        MutableBigInteger a = this;
        int sign = a.compare(b);
        if (sign == 0)
            return 0;
        if (sign < 0) {
            MutableBigInteger tmp = a;
            a = b;
            b = tmp;
        }

        long diff = 0;
        int x = a.intLen;
        int y = b.intLen;

        // Subtract common parts of both numbers
        while (y > 0) {
            x--; y--;
            diff = (a.value[a.offset+ x] & LONG_MASK) -
                (b.value[b.offset+ y] & LONG_MASK) - ((int)-(diff>>32));
            a.value[a.offset+x] = (int)diff;
        }
        // Subtract remainder of longer number
        while (x > 0) {
            x--;
            diff = (a.value[a.offset+ x] & LONG_MASK) - ((int)-(diff>>32));
            a.value[a.offset+x] = (int)diff;
        }

        a.normalize();
        return sign;
    }

    /**
     * Multiply the contents of two MutableBigInteger objects. The result is
     * placed into MutableBigInteger z. The contents of y are not changed.
     */
    void multiply(MutableBigInteger y, MutableBigInteger z) {
        int xLen = intLen;
        int yLen = y.intLen;
        int newLen = xLen + yLen;

        // Put z into an appropriate state to receive product
        if (z.value.length < newLen)
            z.value = new int[newLen];
        z.offset = 0;
        z.intLen = newLen;

        // The first iteration is hoisted out of the loop to avoid extra add
        long carry = 0;
        for (int j=yLen-1, k=yLen+xLen-1; j >= 0; j--, k--) {
                long product = (y.value[j+y.offset] & LONG_MASK) *
                               (value[xLen-1+offset] & LONG_MASK) + carry;
                z.value[k] = (int)product;
                carry = product >>> 32;
        }
        z.value[xLen-1] = (int)carry;

        // Perform the multiplication word by word
        for (int i = xLen-2; i >= 0; i--) {
            carry = 0;
            for (int j=yLen-1, k=yLen+i; j >= 0; j--, k--) {
                long product = (y.value[j+y.offset] & LONG_MASK) *
                               (value[i+offset] & LONG_MASK) +
                               (z.value[k] & LONG_MASK) + carry;
                z.value[k] = (int)product;
                carry = product >>> 32;
            }
            z.value[i] = (int)carry;
        }

        // Remove leading zeros from product
        z.normalize();
    }

    /**
     * Multiply the contents of this MutableBigInteger by the word y. The
     * result is placed into z.
     */
    void mul(int y, MutableBigInteger z) {
        if (y == 1) {
            z.copyValue(this);
            return;
        }

        if (y == 0) {
            z.clear();
            return;
        }

        // Perform the multiplication word by word
        long ylong = y & LONG_MASK;
        int[] zval = (z.value.length < intLen+1 ? new int[intLen + 1]
                                              : z.value);
        long carry = 0;
        for (int i = intLen-1; i >= 0; i--) {
            long product = ylong * (value[i+offset] & LONG_MASK) + carry;
            zval[i+1] = (int)product;
            carry = product >>> 32;
        }

        if (carry == 0) {
            z.offset = 1;
            z.intLen = intLen;
        } else {
            z.offset = 0;
            z.intLen = intLen + 1;
            zval[0] = (int)carry;
        }
        z.value = zval;
    }

     /**
     * This method is used for division of an n word dividend by a one word
     * divisor. The quotient is placed into quotient. The one word divisor is
     * specified by divisor.
     *
     * @return the remainder of the division is returned.
     *
     */
    int divideOneWord(int divisor, MutableBigInteger quotient) {
        long divisorLong = divisor & LONG_MASK;

        // Special case of one word dividend
        if (intLen == 1) {
            long dividendValue = value[offset] & LONG_MASK;
            int q = (int) (dividendValue / divisorLong);
            int r = (int) (dividendValue - q * divisorLong);
            quotient.value[0] = q;
            quotient.intLen = (q == 0) ? 0 : 1;
            quotient.offset = 0;
            return r;
        }

        if (quotient.value.length < intLen)
            quotient.value = new int[intLen];
        quotient.offset = 0;
        quotient.intLen = intLen;

        // Normalize the divisor
        int shift = Integer.numberOfLeadingZeros(divisor);

        int rem = value[offset];
        long remLong = rem & LONG_MASK;
        if (remLong < divisorLong) {
            quotient.value[0] = 0;
        } else {
            quotient.value[0] = (int)(remLong / divisorLong);
            rem = (int) (remLong - (quotient.value[0] * divisorLong));
            remLong = rem & LONG_MASK;
        }
        int xlen = intLen;
        while (--xlen > 0) {
            long dividendEstimate = (remLong << 32) |
                    (value[offset + intLen - xlen] & LONG_MASK);
            int q;
            if (dividendEstimate >= 0) {
                q = (int) (dividendEstimate / divisorLong);
                rem = (int) (dividendEstimate - q * divisorLong);
            } else {
                long tmp = divWord(dividendEstimate, divisor);
                q = (int) (tmp & LONG_MASK);
                rem = (int) (tmp >>> 32);
            }
            quotient.value[intLen - xlen] = q;
            remLong = rem & LONG_MASK;
        }

        quotient.normalize();
        // Unnormalize
        if (shift > 0)
            return rem % divisor;
        else
            return rem;
    }

    /**
     * Calculates the quotient of this div b and places the quotient in the
     * provided MutableBigInteger objects and the remainder object is returned.
     *
     */
    MutableBigInteger divide(MutableBigInteger b, MutableBigInteger quotient) {
        return divide(b,quotient,true);
    }

    MutableBigInteger divide(MutableBigInteger b, MutableBigInteger quotient, boolean needRemainder) {
        if (b.intLen < BigInteger.BURNIKEL_ZIEGLER_THRESHOLD ||
                intLen - b.intLen < BigInteger.BURNIKEL_ZIEGLER_OFFSET) {
            return divideKnuth(b, quotient, needRemainder);
        } else {
            return divideAndRemainderBurnikelZiegler(b, quotient);
        }
    }

    /**
     * @see #divideKnuth(MutableBigInteger, MutableBigInteger, boolean)
     */
    MutableBigInteger divideKnuth(MutableBigInteger b, MutableBigInteger quotient) {
        return divideKnuth(b,quotient,true);
    }

    /**
     * Calculates the quotient of this div b and places the quotient in the
     * provided MutableBigInteger objects and the remainder object is returned.
     *
     * Uses Algorithm D in Knuth section 4.3.1.
     * Many optimizations to that algorithm have been adapted from the Colin
     * Plumb C library.
     * It special cases one word divisors for speed. The content of b is not
     * changed.
     *
     */
    MutableBigInteger divideKnuth(MutableBigInteger b, MutableBigInteger quotient, boolean needRemainder) {
        if (b.intLen == 0)
            throw new ArithmeticException("BigInteger divide by zero");

        // Dividend is zero
        if (intLen == 0) {
            quotient.intLen = quotient.offset = 0;
            return needRemainder ? new MutableBigInteger() : null;
        }

        int cmp = compare(b);
        // Dividend less than divisor
        if (cmp < 0) {
            quotient.intLen = quotient.offset = 0;
            return needRemainder ? new MutableBigInteger(this) : null;
        }
        // Dividend equal to divisor
        if (cmp == 0) {
            quotient.value[0] = quotient.intLen = 1;
            quotient.offset = 0;
            return needRemainder ? new MutableBigInteger() : null;
        }

        quotient.clear();
        // Special case one word divisor
        if (b.intLen == 1) {
            int r = divideOneWord(b.value[b.offset], quotient);
            if(needRemainder) {
                if (r == 0)
                    return new MutableBigInteger();
                return new MutableBigInteger(r);
            } else {
                return null;
            }
        }

        // Cancel common powers of two if we're above the KNUTH_POW2_* thresholds
        if (intLen >= KNUTH_POW2_THRESH_LEN) {
            int trailingZeroBits = Math.min(getLowestSetBit(), b.getLowestSetBit());
            if (trailingZeroBits >= KNUTH_POW2_THRESH_ZEROS*32) {
                MutableBigInteger a = new MutableBigInteger(this);
                b = new MutableBigInteger(b);
                a.rightShift(trailingZeroBits);
                b.rightShift(trailingZeroBits);
                MutableBigInteger r = a.divideKnuth(b, quotient);
                r.leftShift(trailingZeroBits);
                return r;
            }
        }

        return divideMagnitude(b, quotient, needRemainder);
    }

    /**
     * Computes {@code this/b} and {@code this%b} using the
     * <a href="http://cr.yp.to/bib/1998/burnikel.ps"> Burnikel-Ziegler algorithm</a>.
     * This method implements algorithm 3 from pg. 9 of the Burnikel-Ziegler paper.
     * The parameter beta was chosen to b 2<sup>32</sup> so almost all shifts are
     * multiples of 32 bits.<br/>
     * {@code this} and {@code b} must be nonnegative.
     * @param b the divisor
     * @param quotient output parameter for {@code this/b}
     * @return the remainder
     */
    MutableBigInteger divideAndRemainderBurnikelZiegler(MutableBigInteger b, MutableBigInteger quotient) {
        int r = intLen;
        int s = b.intLen;

        // Clear the quotient
        quotient.offset = quotient.intLen = 0;

        if (r < s) {
            return this;
        } else {
            // Unlike Knuth division, we don't check for common powers of two here because
            // BZ already runs faster if both numbers contain powers of two and cancelling them has no
            // additional benefit.

            // step 1: let m = min{2^k | (2^k)*BURNIKEL_ZIEGLER_THRESHOLD > s}
            int m = 1 << (32-Integer.numberOfLeadingZeros(s/BigInteger.BURNIKEL_ZIEGLER_THRESHOLD));

            int j = (s+m-1) / m;      // step 2a: j = ceil(s/m)
            int n = j * m;            // step 2b: block length in 32-bit units
            long n32 = 32L * n;         // block length in bits
            int sigma = (int) Math.max(0, n32 - b.bitLength());   // step 3: sigma = max{T | (2^T)*B < beta^n}
            MutableBigInteger bShifted = new MutableBigInteger(b);
            bShifted.safeLeftShift(sigma);   // step 4a: shift b so its length is a multiple of n
            MutableBigInteger aShifted = new MutableBigInteger (this);
            aShifted.safeLeftShift(sigma);     // step 4b: shift a by the same amount

            // step 5: t is the number of blocks needed to accommodate a plus one additional bit
            int t = (int) ((aShifted.bitLength()+n32) / n32);
            if (t < 2) {
                t = 2;
            }

            // step 6: conceptually split a into blocks a[t-1], ..., a[0]
            MutableBigInteger a1 = aShifted.getBlock(t-1, t, n);   // the most significant block of a

            // step 7: z[t-2] = [a[t-1], a[t-2]]
            MutableBigInteger z = aShifted.getBlock(t-2, t, n);    // the second to most significant block
            z.addDisjoint(a1, n);   // z[t-2]

            // do schoolbook division on blocks, dividing 2-block numbers by 1-block numbers
            MutableBigInteger qi = new MutableBigInteger();
            MutableBigInteger ri;
            for (int i=t-2; i > 0; i--) {
                // step 8a: compute (qi,ri) such that z=b*qi+ri
                ri = z.divide2n1n(bShifted, qi);

                // step 8b: z = [ri, a[i-1]]
                z = aShifted.getBlock(i-1, t, n);   // a[i-1]
                z.addDisjoint(ri, n);
                quotient.addShifted(qi, i*n);   // update q (part of step 9)
            }
            // final iteration of step 8: do the loop one more time for i=0 but leave z unchanged
            ri = z.divide2n1n(bShifted, qi);
            quotient.add(qi);

            ri.rightShift(sigma);   // step 9: a and b were shifted, so shift back
            return ri;
        }
    }

    /**
     * This method implements algorithm 1 from pg. 4 of the Burnikel-Ziegler paper.
     * It divides a 2n-digit number by a n-digit number.<br/>
     * The parameter beta is 2<sup>32</sup> so all shifts are multiples of 32 bits.
     * <br/>
     * {@code this} must be a nonnegative number such that {@code this.bitLength() <= 2*b.bitLength()}
     * @param b a positive number such that {@code b.bitLength()} is even
     * @param quotient output parameter for {@code this/b}
     * @return {@code this%b}
     */
    private MutableBigInteger divide2n1n(MutableBigInteger b, MutableBigInteger quotient) {
        int n = b.intLen;

        // step 1: base case
        if (n%2 != 0 || n < BigInteger.BURNIKEL_ZIEGLER_THRESHOLD) {
            return divideKnuth(b, quotient);
        }

        // step 2: view this as [a1,a2,a3,a4] where each ai is n/2 ints or less
        MutableBigInteger aUpper = new MutableBigInteger(this);
        aUpper.safeRightShift(32*(n/2));   // aUpper = [a1,a2,a3]
        keepLower(n/2);   // this = a4

        // step 3: q1=aUpper/b, r1=aUpper%b
        MutableBigInteger q1 = new MutableBigInteger();
        MutableBigInteger r1 = aUpper.divide3n2n(b, q1);

        // step 4: quotient=[r1,this]/b, r2=[r1,this]%b
        addDisjoint(r1, n/2);   // this = [r1,this]
        MutableBigInteger r2 = divide3n2n(b, quotient);

        // step 5: let quotient=[q1,quotient] and return r2
        quotient.addDisjoint(q1, n/2);
        return r2;
    }

    /**
     * This method implements algorithm 2 from pg. 5 of the Burnikel-Ziegler paper.
     * It divides a 3n-digit number by a 2n-digit number.<br/>
     * The parameter beta is 2<sup>32</sup> so all shifts are multiples of 32 bits.<br/>
     * <br/>
     * {@code this} must be a nonnegative number such that {@code 2*this.bitLength() <= 3*b.bitLength()}
     * @param quotient output parameter for {@code this/b}
     * @return {@code this%b}
     */
    private MutableBigInteger divide3n2n(MutableBigInteger b, MutableBigInteger quotient) {
        int n = b.intLen / 2;   // half the length of b in ints

        // step 1: view this as [a1,a2,a3] where each ai is n ints or less; let a12=[a1,a2]
        MutableBigInteger a12 = new MutableBigInteger(this);
        a12.safeRightShift(32*n);

        // step 2: view b as [b1,b2] where each bi is n ints or less
        MutableBigInteger b1 = new MutableBigInteger(b);
        b1.safeRightShift(n * 32);
        BigInteger b2 = b.getLower(n);

        MutableBigInteger r;
        MutableBigInteger d;
        if (compareShifted(b, n) < 0) {
            // step 3a: if a1<b1, let quotient=a12/b1 and r=a12%b1
            r = a12.divide2n1n(b1, quotient);

            // step 4: d=quotient*b2
            d = new MutableBigInteger(quotient.toBigInteger().multiply(b2));
        } else {
            // step 3b: if a1>=b1, let quotient=beta^n-1 and r=a12-b1*2^n+b1
            quotient.ones(n);
            a12.add(b1);
            b1.leftShift(32*n);
            a12.subtract(b1);
            r = a12;

            // step 4: d=quotient*b2=(b2 << 32*n) - b2
            d = new MutableBigInteger(b2);
            d.leftShift(32 * n);
            d.subtract(new MutableBigInteger(b2));
        }

        // step 5: r = r*beta^n + a3 - d (paper says a4)
        // However, don't subtract d until after the while loop so r doesn't become negative
        r.leftShift(32 * n);
        r.addLower(this, n);

        // step 6: add b until r>=d
        while (r.compare(d) < 0) {
            r.add(b);
            quotient.subtract(MutableBigInteger.ONE);
        }
        r.subtract(d);

        return r;
    }

    /**
     * Returns a {@code MutableBigInteger} containing {@code blockLength} ints from
     * {@code this} number, starting at {@code index*blockLength}.<br/>
     * Used by Burnikel-Ziegler division.
     * @param index the block index
     * @param numBlocks the total number of blocks in {@code this} number
     * @param blockLength length of one block in units of 32 bits
     * @return
     */
    private MutableBigInteger getBlock(int index, int numBlocks, int blockLength) {
        int blockStart = index * blockLength;
        if (blockStart >= intLen) {
            return new MutableBigInteger();
        }

        int blockEnd;
        if (index == numBlocks-1) {
            blockEnd = intLen;
        } else {
            blockEnd = (index+1) * blockLength;
        }
        if (blockEnd > intLen) {
            return new MutableBigInteger();
        }

        int[] newVal = Arrays.copyOfRange(value, offset+intLen-blockEnd, offset+intLen-blockStart);
        return new MutableBigInteger(newVal);
    }

    /** @see BigInteger#bitLength() */
    long bitLength() {
        if (intLen == 0)
            return 0;
        return intLen*32L - Integer.numberOfLeadingZeros(value[offset]);
    }

    /**
     * Internally used  to calculate the quotient of this div v and places the
     * quotient in the provided MutableBigInteger object and the remainder is
     * returned.
     *
     * @return the remainder of the division will be returned.
     */
    long divide(long v, MutableBigInteger quotient) {
        if (v == 0)
            throw new ArithmeticException("BigInteger divide by zero");

        // Dividend is zero
        if (intLen == 0) {
            quotient.intLen = quotient.offset = 0;
            return 0;
        }
        if (v < 0)
            v = -v;

        int d = (int)(v >>> 32);
        quotient.clear();
        // Special case on word divisor
        if (d == 0)
            return divideOneWord((int)v, quotient) & LONG_MASK;
        else {
            return divideLongMagnitude(v, quotient).toLong();
        }
    }

    private static void copyAndShift(int[] src, int srcFrom, int srcLen, int[] dst, int dstFrom, int shift) {
        int n2 = 32 - shift;
        int c=src[srcFrom];
        for (int i=0; i < srcLen-1; i++) {
            int b = c;
            c = src[++srcFrom];
            dst[dstFrom+i] = (b << shift) | (c >>> n2);
        }
        dst[dstFrom+srcLen-1] = c << shift;
    }

    /**
     * Divide this MutableBigInteger by the divisor.
     * The quotient will be placed into the provided quotient object &
     * the remainder object is returned.
     */
    private MutableBigInteger divideMagnitude(MutableBigInteger div,
                                              MutableBigInteger quotient,
                                              boolean needRemainder ) {
        // assert div.intLen > 1
        // D1 normalize the divisor
        int shift = Integer.numberOfLeadingZeros(div.value[div.offset]);
        // Copy divisor value to protect divisor
        final int dlen = div.intLen;
        int[] divisor;
        MutableBigInteger rem; // Remainder starts as dividend with space for a leading zero
        if (shift > 0) {
            divisor = new int[dlen];
            copyAndShift(div.value,div.offset,dlen,divisor,0,shift);
            if (Integer.numberOfLeadingZeros(value[offset]) >= shift) {
                int[] remarr = new int[intLen + 1];
                rem = new MutableBigInteger(remarr);
                rem.intLen = intLen;
                rem.offset = 1;
                copyAndShift(value,offset,intLen,remarr,1,shift);
            } else {
                int[] remarr = new int[intLen + 2];
                rem = new MutableBigInteger(remarr);
                rem.intLen = intLen+1;
                rem.offset = 1;
                int rFrom = offset;
                int c=0;
                int n2 = 32 - shift;
                for (int i=1; i < intLen+1; i++,rFrom++) {
                    int b = c;
                    c = value[rFrom];
                    remarr[i] = (b << shift) | (c >>> n2);
                }
                remarr[intLen+1] = c << shift;
            }
        } else {
            divisor = Arrays.copyOfRange(div.value, div.offset, div.offset + div.intLen);
            rem = new MutableBigInteger(new int[intLen + 1]);
            System.arraycopy(value, offset, rem.value, 1, intLen);
            rem.intLen = intLen;
            rem.offset = 1;
        }

        int nlen = rem.intLen;

        // Set the quotient size
        final int limit = nlen - dlen + 1;
        if (quotient.value.length < limit) {
            quotient.value = new int[limit];
            quotient.offset = 0;
        }
        quotient.intLen = limit;
        int[] q = quotient.value;


        // Must insert leading 0 in rem if its length did not change
        if (rem.intLen == nlen) {
            rem.offset = 0;
            rem.value[0] = 0;
            rem.intLen++;
        }

        int dh = divisor[0];
        long dhLong = dh & LONG_MASK;
        int dl = divisor[1];

        // D2 Initialize j
        for (int j=0; j < limit-1; j++) {
            // D3 Calculate qhat
            // estimate qhat
            int qhat = 0;
            int qrem = 0;
            boolean skipCorrection = false;
            int nh = rem.value[j+rem.offset];
            int nh2 = nh + 0x80000000;
            int nm = rem.value[j+1+rem.offset];

            if (nh == dh) {
                qhat = ~0;
                qrem = nh + nm;
                skipCorrection = qrem + 0x80000000 < nh2;
            } else {
                long nChunk = (((long)nh) << 32) | (nm & LONG_MASK);
                if (nChunk >= 0) {
                    qhat = (int) (nChunk / dhLong);
                    qrem = (int) (nChunk - (qhat * dhLong));
                } else {
                    long tmp = divWord(nChunk, dh);
                    qhat = (int) (tmp & LONG_MASK);
                    qrem = (int) (tmp >>> 32);
                }
            }

            if (qhat == 0)
                continue;

            if (!skipCorrection) { // Correct qhat
                long nl = rem.value[j+2+rem.offset] & LONG_MASK;
                long rs = ((qrem & LONG_MASK) << 32) | nl;
                long estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);

                if (unsignedLongCompare(estProduct, rs)) {
                    qhat--;
                    qrem = (int)((qrem & LONG_MASK) + dhLong);
                    if ((qrem & LONG_MASK) >=  dhLong) {
                        estProduct -= (dl & LONG_MASK);
                        rs = ((qrem & LONG_MASK) << 32) | nl;
                        if (unsignedLongCompare(estProduct, rs))
                            qhat--;
                    }
                }
            }

            // D4 Multiply and subtract
            rem.value[j+rem.offset] = 0;
            int borrow = mulsub(rem.value, divisor, qhat, dlen, j+rem.offset);

            // D5 Test remainder
            if (borrow + 0x80000000 > nh2) {
                // D6 Add back
                divadd(divisor, rem.value, j+1+rem.offset);
                qhat--;
            }

            // Store the quotient digit
            q[j] = qhat;
        } // D7 loop on j
        // D3 Calculate qhat
        // estimate qhat
        int qhat = 0;
        int qrem = 0;
        boolean skipCorrection = false;
        int nh = rem.value[limit - 1 + rem.offset];
        int nh2 = nh + 0x80000000;
        int nm = rem.value[limit + rem.offset];

        if (nh == dh) {
            qhat = ~0;
            qrem = nh + nm;
            skipCorrection = qrem + 0x80000000 < nh2;
        } else {
            long nChunk = (((long) nh) << 32) | (nm & LONG_MASK);
            if (nChunk >= 0) {
                qhat = (int) (nChunk / dhLong);
                qrem = (int) (nChunk - (qhat * dhLong));
            } else {
                long tmp = divWord(nChunk, dh);
                qhat = (int) (tmp & LONG_MASK);
                qrem = (int) (tmp >>> 32);
            }
        }
        if (qhat != 0) {
            if (!skipCorrection) { // Correct qhat
                long nl = rem.value[limit + 1 + rem.offset] & LONG_MASK;
                long rs = ((qrem & LONG_MASK) << 32) | nl;
                long estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);

                if (unsignedLongCompare(estProduct, rs)) {
                    qhat--;
                    qrem = (int) ((qrem & LONG_MASK) + dhLong);
                    if ((qrem & LONG_MASK) >= dhLong) {
                        estProduct -= (dl & LONG_MASK);
                        rs = ((qrem & LONG_MASK) << 32) | nl;
                        if (unsignedLongCompare(estProduct, rs))
                            qhat--;
                    }
                }
            }


            // D4 Multiply and subtract
            int borrow;
            rem.value[limit - 1 + rem.offset] = 0;
            if(needRemainder)
                borrow = mulsub(rem.value, divisor, qhat, dlen, limit - 1 + rem.offset);
            else
                borrow = mulsubBorrow(rem.value, divisor, qhat, dlen, limit - 1 + rem.offset);

            // D5 Test remainder
            if (borrow + 0x80000000 > nh2) {
                // D6 Add back
                if(needRemainder)
                    divadd(divisor, rem.value, limit - 1 + 1 + rem.offset);
                qhat--;
            }

            // Store the quotient digit
            q[(limit - 1)] = qhat;
        }


        if (needRemainder) {
            // D8 Unnormalize
            if (shift > 0)
                rem.rightShift(shift);
            rem.normalize();
        }
        quotient.normalize();
        return needRemainder ? rem : null;
    }

    /**
     * Divide this MutableBigInteger by the divisor represented by positive long
     * value. The quotient will be placed into the provided quotient object &
     * the remainder object is returned.
     */
    private MutableBigInteger divideLongMagnitude(long ldivisor, MutableBigInteger quotient) {
        // Remainder starts as dividend with space for a leading zero
        MutableBigInteger rem = new MutableBigInteger(new int[intLen + 1]);
        System.arraycopy(value, offset, rem.value, 1, intLen);
        rem.intLen = intLen;
        rem.offset = 1;

        int nlen = rem.intLen;

        int limit = nlen - 2 + 1;
        if (quotient.value.length < limit) {
            quotient.value = new int[limit];
            quotient.offset = 0;
        }
        quotient.intLen = limit;
        int[] q = quotient.value;

        // D1 normalize the divisor
        int shift = Long.numberOfLeadingZeros(ldivisor);
        if (shift > 0) {
            ldivisor<<=shift;
            rem.leftShift(shift);
        }

        // Must insert leading 0 in rem if its length did not change
        if (rem.intLen == nlen) {
            rem.offset = 0;
            rem.value[0] = 0;
            rem.intLen++;
        }

        int dh = (int)(ldivisor >>> 32);
        long dhLong = dh & LONG_MASK;
        int dl = (int)(ldivisor & LONG_MASK);

        // D2 Initialize j
        for (int j = 0; j < limit; j++) {
            // D3 Calculate qhat
            // estimate qhat
            int qhat = 0;
            int qrem = 0;
            boolean skipCorrection = false;
            int nh = rem.value[j + rem.offset];
            int nh2 = nh + 0x80000000;
            int nm = rem.value[j + 1 + rem.offset];

            if (nh == dh) {
                qhat = ~0;
                qrem = nh + nm;
                skipCorrection = qrem + 0x80000000 < nh2;
            } else {
                long nChunk = (((long) nh) << 32) | (nm & LONG_MASK);
                if (nChunk >= 0) {
                    qhat = (int) (nChunk / dhLong);
                    qrem = (int) (nChunk - (qhat * dhLong));
                } else {
                    long tmp = divWord(nChunk, dh);
                    qhat =(int)(tmp & LONG_MASK);
                    qrem = (int)(tmp>>>32);
                }
            }

            if (qhat == 0)
                continue;

            if (!skipCorrection) { // Correct qhat
                long nl = rem.value[j + 2 + rem.offset] & LONG_MASK;
                long rs = ((qrem & LONG_MASK) << 32) | nl;
                long estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);

                if (unsignedLongCompare(estProduct, rs)) {
                    qhat--;
                    qrem = (int) ((qrem & LONG_MASK) + dhLong);
                    if ((qrem & LONG_MASK) >= dhLong) {
                        estProduct -= (dl & LONG_MASK);
                        rs = ((qrem & LONG_MASK) << 32) | nl;
                        if (unsignedLongCompare(estProduct, rs))
                            qhat--;
                    }
                }
            }

            // D4 Multiply and subtract
            rem.value[j + rem.offset] = 0;
            int borrow = mulsubLong(rem.value, dh, dl, qhat,  j + rem.offset);

            // D5 Test remainder
            if (borrow + 0x80000000 > nh2) {
                // D6 Add back
                divaddLong(dh,dl, rem.value, j + 1 + rem.offset);
                qhat--;
            }

            // Store the quotient digit
            q[j] = qhat;
        } // D7 loop on j

        // D8 Unnormalize
        if (shift > 0)
            rem.rightShift(shift);

        quotient.normalize();
        rem.normalize();
        return rem;
    }

    /**
     * A primitive used for division by long.
     * Specialized version of the method divadd.
     * dh is a high part of the divisor, dl is a low part
     */
    private int divaddLong(int dh, int dl, int[] result, int offset) {
        long carry = 0;

        long sum = (dl & LONG_MASK) + (result[1+offset] & LONG_MASK);
        result[1+offset] = (int)sum;

        sum = (dh & LONG_MASK) + (result[offset] & LONG_MASK) + carry;
        result[offset] = (int)sum;
        carry = sum >>> 32;
        return (int)carry;
    }

    /**
     * This method is used for division by long.
     * Specialized version of the method sulsub.
     * dh is a high part of the divisor, dl is a low part
     */
    private int mulsubLong(int[] q, int dh, int dl, int x, int offset) {
        long xLong = x & LONG_MASK;
        offset += 2;
        long product = (dl & LONG_MASK) * xLong;
        long difference = q[offset] - product;
        q[offset--] = (int)difference;
        long carry = (product >>> 32)
                 + (((difference & LONG_MASK) >
                     (((~(int)product) & LONG_MASK))) ? 1:0);
        product = (dh & LONG_MASK) * xLong + carry;
        difference = q[offset] - product;
        q[offset--] = (int)difference;
        carry = (product >>> 32)
                 + (((difference & LONG_MASK) >
                     (((~(int)product) & LONG_MASK))) ? 1:0);
        return (int)carry;
    }

    /**
     * Compare two longs as if they were unsigned.
     * Returns true iff one is bigger than two.
     */
    private boolean unsignedLongCompare(long one, long two) {
        return (one+Long.MIN_VALUE) > (two+Long.MIN_VALUE);
    }

    /**
     * This method divides a long quantity by an int to estimate
     * qhat for two multi precision numbers. It is used when
     * the signed value of n is less than zero.
     * Returns long value where high 32 bits contain remainder value and
     * low 32 bits contain quotient value.
     */
    static long divWord(long n, int d) {
        long dLong = d & LONG_MASK;
        long r;
        long q;
        if (dLong == 1) {
            q = (int)n;
            r = 0;
            return (r << 32) | (q & LONG_MASK);
        }

        // Approximate the quotient and remainder
        q = (n >>> 1) / (dLong >>> 1);
        r = n - q*dLong;

        // Correct the approximation
        while (r < 0) {
            r += dLong;
            q--;
        }
        while (r >= dLong) {
            r -= dLong;
            q++;
        }
        // n - q*dlong == r && 0 <= r <dLong, hence we're done.
        return (r << 32) | (q & LONG_MASK);
    }

    /**
     * Calculate GCD of this and b. This and b are changed by the computation.
     */
    MutableBigInteger hybridGCD(MutableBigInteger b) {
        // Use Euclid's algorithm until the numbers are approximately the
        // same length, then use the binary GCD algorithm to find the GCD.
        MutableBigInteger a = this;
        MutableBigInteger q = new MutableBigInteger();

        while (b.intLen != 0) {
            if (Math.abs(a.intLen - b.intLen) < 2)
                return a.binaryGCD(b);

            MutableBigInteger r = a.divide(b, q);
            a = b;
            b = r;
        }
        return a;
    }

    /**
     * Calculate GCD of this and v.
     * Assumes that this and v are not zero.
     */
    private MutableBigInteger binaryGCD(MutableBigInteger v) {
        // Algorithm B from Knuth section 4.5.2
        MutableBigInteger u = this;
        MutableBigInteger r = new MutableBigInteger();

        // step B1
        int s1 = u.getLowestSetBit();
        int s2 = v.getLowestSetBit();
        int k = (s1 < s2) ? s1 : s2;
        if (k != 0) {
            u.rightShift(k);
            v.rightShift(k);
        }

        // step B2
        boolean uOdd = (k == s1);
        MutableBigInteger t = uOdd ? v: u;
        int tsign = uOdd ? -1 : 1;

        int lb;
        while ((lb = t.getLowestSetBit()) >= 0) {
            // steps B3 and B4
            t.rightShift(lb);
            // step B5
            if (tsign > 0)
                u = t;
            else
                v = t;

            // Special case one word numbers
            if (u.intLen < 2 && v.intLen < 2) {
                int x = u.value[u.offset];
                int y = v.value[v.offset];
                x  = binaryGcd(x, y);
                r.value[0] = x;
                r.intLen = 1;
                r.offset = 0;
                if (k > 0)
                    r.leftShift(k);
                return r;
            }

            // step B6
            if ((tsign = u.difference(v)) == 0)
                break;
            t = (tsign >= 0) ? u : v;
        }

        if (k > 0)
            u.leftShift(k);
        return u;
    }

    /**
     * Calculate GCD of a and b interpreted as unsigned integers.
     */
    static int binaryGcd(int a, int b) {
        if (b == 0)
            return a;
        if (a == 0)
            return b;

        // Right shift a & b till their last bits equal to 1.
        int aZeros = Integer.numberOfTrailingZeros(a);
        int bZeros = Integer.numberOfTrailingZeros(b);
        a >>>= aZeros;
        b >>>= bZeros;

        int t = (aZeros < bZeros ? aZeros : bZeros);

        while (a != b) {
            if ((a+0x80000000) > (b+0x80000000)) {  // a > b as unsigned
                a -= b;
                a >>>= Integer.numberOfTrailingZeros(a);
            } else {
                b -= a;
                b >>>= Integer.numberOfTrailingZeros(b);
            }
        }
        return a<<t;
    }

    /**
     * Returns the modInverse of this mod p. This and p are not affected by
     * the operation.
     */
    MutableBigInteger mutableModInverse(MutableBigInteger p) {
        // Modulus is odd, use Schroeppel's algorithm
        if (p.isOdd())
            return modInverse(p);

        // Base and modulus are even, throw exception
        if (isEven())
            throw new ArithmeticException("BigInteger not invertible.");

        // Get even part of modulus expressed as a power of 2
        int powersOf2 = p.getLowestSetBit();

        // Construct odd part of modulus
        MutableBigInteger oddMod = new MutableBigInteger(p);
        oddMod.rightShift(powersOf2);

        if (oddMod.isOne())
            return modInverseMP2(powersOf2);

        // Calculate 1/a mod oddMod
        MutableBigInteger oddPart = modInverse(oddMod);

        // Calculate 1/a mod evenMod
        MutableBigInteger evenPart = modInverseMP2(powersOf2);

        // Combine the results using Chinese Remainder Theorem
        MutableBigInteger y1 = modInverseBP2(oddMod, powersOf2);
        MutableBigInteger y2 = oddMod.modInverseMP2(powersOf2);

        MutableBigInteger temp1 = new MutableBigInteger();
        MutableBigInteger temp2 = new MutableBigInteger();
        MutableBigInteger result = new MutableBigInteger();

        oddPart.leftShift(powersOf2);
        oddPart.multiply(y1, result);

        evenPart.multiply(oddMod, temp1);
        temp1.multiply(y2, temp2);

        result.add(temp2);
        return result.divide(p, temp1);
    }

    /*
     * Calculate the multiplicative inverse of this mod 2^k.
     */
    MutableBigInteger modInverseMP2(int k) {
        if (isEven())
            throw new ArithmeticException("Non-invertible. (GCD != 1)");

        if (k > 64)
            return euclidModInverse(k);

        int t = inverseMod32(value[offset+intLen-1]);

        if (k < 33) {
            t = (k == 32 ? t : t & ((1 << k) - 1));
            return new MutableBigInteger(t);
        }

        long pLong = (value[offset+intLen-1] & LONG_MASK);
        if (intLen > 1)
            pLong |=  ((long)value[offset+intLen-2] << 32);
        long tLong = t & LONG_MASK;
        tLong = tLong * (2 - pLong * tLong);  // 1 more Newton iter step
        tLong = (k == 64 ? tLong : tLong & ((1L << k) - 1));

        MutableBigInteger result = new MutableBigInteger(new int[2]);
        result.value[0] = (int)(tLong >>> 32);
        result.value[1] = (int)tLong;
        result.intLen = 2;
        result.normalize();
        return result;
    }

    /**
     * Returns the multiplicative inverse of val mod 2^32.  Assumes val is odd.
     */
    static int inverseMod32(int val) {
        // Newton's iteration!
        int t = val;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        return t;
    }

    /**
     * Returns the multiplicative inverse of val mod 2^64.  Assumes val is odd.
     */
    static long inverseMod64(long val) {
        // Newton's iteration!
        long t = val;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        t *= 2 - val*t;
        assert(t * val == 1);
        return t;
    }

    /**
     * Calculate the multiplicative inverse of 2^k mod mod, where mod is odd.
     */
    static MutableBigInteger modInverseBP2(MutableBigInteger mod, int k) {
        // Copy the mod to protect original
        return fixup(new MutableBigInteger(1), new MutableBigInteger(mod), k);
    }

    /**
     * Calculate the multiplicative inverse of this modulo mod, where the mod
     * argument is odd.  This and mod are not changed by the calculation.
     *
     * This method implements an algorithm due to Richard Schroeppel, that uses
     * the same intermediate representation as Montgomery Reduction
     * ("Montgomery Form").  The algorithm is described in an unpublished
     * manuscript entitled "Fast Modular Reciprocals."
     */
    private MutableBigInteger modInverse(MutableBigInteger mod) {
        MutableBigInteger p = new MutableBigInteger(mod);
        MutableBigInteger f = new MutableBigInteger(this);
        MutableBigInteger g = new MutableBigInteger(p);
        SignedMutableBigInteger c = new SignedMutableBigInteger(1);
        SignedMutableBigInteger d = new SignedMutableBigInteger();
        MutableBigInteger temp = null;
        SignedMutableBigInteger sTemp = null;

        int k = 0;
        // Right shift f k times until odd, left shift d k times
        if (f.isEven()) {
            int trailingZeros = f.getLowestSetBit();
            f.rightShift(trailingZeros);
            d.leftShift(trailingZeros);
            k = trailingZeros;
        }

        // The Almost Inverse Algorithm
        while (!f.isOne()) {
            // If gcd(f, g) != 1, number is not invertible modulo mod
            if (f.isZero())
                throw new ArithmeticException("BigInteger not invertible.");

            // If f < g exchange f, g and c, d
            if (f.compare(g) < 0) {
                temp = f; f = g; g = temp;
                sTemp = d; d = c; c = sTemp;
            }

            // If f == g (mod 4)
            if (((f.value[f.offset + f.intLen - 1] ^
                 g.value[g.offset + g.intLen - 1]) & 3) == 0) {
                f.subtract(g);
                c.signedSubtract(d);
            } else { // If f != g (mod 4)
                f.add(g);
                c.signedAdd(d);
            }

            // Right shift f k times until odd, left shift d k times
            int trailingZeros = f.getLowestSetBit();
            f.rightShift(trailingZeros);
            d.leftShift(trailingZeros);
            k += trailingZeros;
        }

        if (c.compare(p) >= 0) { // c has a larger magnitude than p
            MutableBigInteger remainder = c.divide(p,
                new MutableBigInteger());
            // The previous line ignores the sign so we copy the data back
            // into c which will restore the sign as needed (and converts
            // it back to a SignedMutableBigInteger)
            c.copyValue(remainder);
        }

        if (c.sign < 0) {
           c.signedAdd(p);
        }

        return fixup(c, p, k);
    }

    /**
     * The Fixup Algorithm
     * Calculates X such that X = C * 2^(-k) (mod P)
     * Assumes C<P and P is odd.
     */
    static MutableBigInteger fixup(MutableBigInteger c, MutableBigInteger p,
                                                                      int k) {
        MutableBigInteger temp = new MutableBigInteger();
        // Set r to the multiplicative inverse of p mod 2^32
        int r = -inverseMod32(p.value[p.offset+p.intLen-1]);

        for (int i=0, numWords = k >> 5; i < numWords; i++) {
            // V = R * c (mod 2^j)
            int  v = r * c.value[c.offset + c.intLen-1];
            // c = c + (v * p)
            p.mul(v, temp);
            c.add(temp);
            // c = c / 2^j
            c.intLen--;
        }
        int numBits = k & 0x1f;
        if (numBits != 0) {
            // V = R * c (mod 2^j)
            int v = r * c.value[c.offset + c.intLen-1];
            v &= ((1<<numBits) - 1);
            // c = c + (v * p)
            p.mul(v, temp);
            c.add(temp);
            // c = c / 2^j
            c.rightShift(numBits);
        }

        // In theory, c may be greater than p at this point (Very rare!)
        if (c.compare(p) >= 0)
            c = c.divide(p, new MutableBigInteger());

        return c;
    }

    /**
     * Uses the extended Euclidean algorithm to compute the modInverse of base
     * mod a modulus that is a power of 2. The modulus is 2^k.
     */
    MutableBigInteger euclidModInverse(int k) {
        MutableBigInteger b = new MutableBigInteger(1);
        b.leftShift(k);
        MutableBigInteger mod = new MutableBigInteger(b);

        MutableBigInteger a = new MutableBigInteger(this);
        MutableBigInteger q = new MutableBigInteger();
        MutableBigInteger r = b.divide(a, q);

        MutableBigInteger swapper = b;
        // swap b & r
        b = r;
        r = swapper;

        MutableBigInteger t1 = new MutableBigInteger(q);
        MutableBigInteger t0 = new MutableBigInteger(1);
        MutableBigInteger temp = new MutableBigInteger();

        while (!b.isOne()) {
            r = a.divide(b, q);

            if (r.intLen == 0)
                throw new ArithmeticException("BigInteger not invertible.");

            swapper = r;
            a = swapper;

            if (q.intLen == 1)
                t1.mul(q.value[q.offset], temp);
            else
                q.multiply(t1, temp);
            swapper = q;
            q = temp;
            temp = swapper;
            t0.add(q);

            if (a.isOne())
                return t0;

            r = b.divide(a, q);

            if (r.intLen == 0)
                throw new ArithmeticException("BigInteger not invertible.");

            swapper = b;
            b =  r;

            if (q.intLen == 1)
                t0.mul(q.value[q.offset], temp);
            else
                q.multiply(t0, temp);
            swapper = q; q = temp; temp = swapper;

            t1.add(q);
        }
        mod.subtract(t1);
        return mod;
    }
}
/*
 * Copyright (c) 1998, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/**
 * Provides classes for performing arbitrary-precision integer
 * arithmetic ({@code BigInteger}) and arbitrary-precision decimal
 * arithmetic ({@code BigDecimal}).  {@code BigInteger} is analogous
 * to the primitive integer types except that it provides arbitrary
 * precision, hence operations on {@code BigInteger}s do not overflow
 * or lose precision.  In addition to standard arithmetic operations,
 * {@code BigInteger} provides modular arithmetic, GCD calculation,
 * primality testing, prime generation, bit manipulation, and a few
 * other miscellaneous operations.
 *
 * {@code BigDecimal} provides arbitrary-precision signed decimal
 * numbers suitable for currency calculations and the like.  {@code
 * BigDecimal} gives the user complete control over rounding behavior,
 * allowing the user to choose from a comprehensive set of eight
 * rounding modes.
 *
 * @since JDK1.1
 */
package java.math;
/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 * Portions Copyright IBM Corporation, 2001. All Rights Reserved.
 */
package java.math;

/**
 * Specifies a <i>rounding behavior</i> for numerical operations
 * capable of discarding precision. Each rounding mode indicates how
 * the least significant returned digit of a rounded result is to be
 * calculated.  If fewer digits are returned than the digits needed to
 * represent the exact numerical result, the discarded digits will be
 * referred to as the <i>discarded fraction</i> regardless the digits'
 * contribution to the value of the number.  In other words,
 * considered as a numerical value, the discarded fraction could have
 * an absolute value greater than one.
 *
 * <p>Each rounding mode description includes a table listing how
 * different two-digit decimal values would round to a one digit
 * decimal value under the rounding mode in question.  The result
 * column in the tables could be gotten by creating a
 * {@code BigDecimal} number with the specified value, forming a
 * {@link MathContext} object with the proper settings
 * ({@code precision} set to {@code 1}, and the
 * {@code roundingMode} set to the rounding mode in question), and
 * calling {@link BigDecimal#round round} on this number with the
 * proper {@code MathContext}.  A summary table showing the results
 * of these rounding operations for all rounding modes appears below.
 *
 *<table border>
 * <caption><b>Summary of Rounding Operations Under Different Rounding Modes</b></caption>
 * <tr><th></th><th colspan=8>Result of rounding input to one digit with the given
 *                           rounding mode</th>
 * <tr valign=top>
 * <th>Input Number</th>         <th>{@code UP}</th>
 *                                           <th>{@code DOWN}</th>
 *                                                        <th>{@code CEILING}</th>
 *                                                                       <th>{@code FLOOR}</th>
 *                                                                                    <th>{@code HALF_UP}</th>
 *                                                                                                   <th>{@code HALF_DOWN}</th>
 *                                                                                                                    <th>{@code HALF_EVEN}</th>
 *                                                                                                                                     <th>{@code UNNECESSARY}</th>
 *
 * <tr align=right><td>5.5</td>  <td>6</td>  <td>5</td>    <td>6</td>    <td>5</td>  <td>6</td>      <td>5</td>       <td>6</td>       <td>throw {@code ArithmeticException}</td>
 * <tr align=right><td>2.5</td>  <td>3</td>  <td>2</td>    <td>3</td>    <td>2</td>  <td>3</td>      <td>2</td>       <td>2</td>       <td>throw {@code ArithmeticException}</td>
 * <tr align=right><td>1.6</td>  <td>2</td>  <td>1</td>    <td>2</td>    <td>1</td>  <td>2</td>      <td>2</td>       <td>2</td>       <td>throw {@code ArithmeticException}</td>
 * <tr align=right><td>1.1</td>  <td>2</td>  <td>1</td>    <td>2</td>    <td>1</td>  <td>1</td>      <td>1</td>       <td>1</td>       <td>throw {@code ArithmeticException}</td>
 * <tr align=right><td>1.0</td>  <td>1</td>  <td>1</td>    <td>1</td>    <td>1</td>  <td>1</td>      <td>1</td>       <td>1</td>       <td>1</td>
 * <tr align=right><td>-1.0</td> <td>-1</td> <td>-1</td>   <td>-1</td>   <td>-1</td> <td>-1</td>     <td>-1</td>      <td>-1</td>      <td>-1</td>
 * <tr align=right><td>-1.1</td> <td>-2</td> <td>-1</td>   <td>-1</td>   <td>-2</td> <td>-1</td>     <td>-1</td>      <td>-1</td>      <td>throw {@code ArithmeticException}</td>
 * <tr align=right><td>-1.6</td> <td>-2</td> <td>-1</td>   <td>-1</td>   <td>-2</td> <td>-2</td>     <td>-2</td>      <td>-2</td>      <td>throw {@code ArithmeticException}</td>
 * <tr align=right><td>-2.5</td> <td>-3</td> <td>-2</td>   <td>-2</td>   <td>-3</td> <td>-3</td>     <td>-2</td>      <td>-2</td>      <td>throw {@code ArithmeticException}</td>
 * <tr align=right><td>-5.5</td> <td>-6</td> <td>-5</td>   <td>-5</td>   <td>-6</td> <td>-6</td>     <td>-5</td>      <td>-6</td>      <td>throw {@code ArithmeticException}</td>
 *</table>
 *
 *
 * <p>This {@code enum} is intended to replace the integer-based
 * enumeration of rounding mode constants in {@link BigDecimal}
 * ({@link BigDecimal#ROUND_UP}, {@link BigDecimal#ROUND_DOWN},
 * etc. ).
 *
 * @see     BigDecimal
 * @see     MathContext
 * @author  Josh Bloch
 * @author  Mike Cowlishaw
 * @author  Joseph D. Darcy
 * @since 1.5
 */
public enum RoundingMode {

        /**
         * Rounding mode to round away from zero.  Always increments the
         * digit prior to a non-zero discarded fraction.  Note that this
         * rounding mode never decreases the magnitude of the calculated
         * value.
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode UP Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code UP} rounding
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>3</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>2</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-2</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-3</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    UP(BigDecimal.ROUND_UP),

        /**
         * Rounding mode to round towards zero.  Never increments the digit
         * prior to a discarded fraction (i.e., truncates).  Note that this
         * rounding mode never increases the magnitude of the calculated value.
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode DOWN Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code DOWN} rounding
         *<tr align=right><td>5.5</td>  <td>5</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>1</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-1</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-5</td>
         *</table>
         */
    DOWN(BigDecimal.ROUND_DOWN),

        /**
         * Rounding mode to round towards positive infinity.  If the
         * result is positive, behaves as for {@code RoundingMode.UP};
         * if negative, behaves as for {@code RoundingMode.DOWN}.  Note
         * that this rounding mode never decreases the calculated value.
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode CEILING Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code CEILING} rounding
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>3</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>2</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-1</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-5</td>
         *</table>
         */
    CEILING(BigDecimal.ROUND_CEILING),

        /**
         * Rounding mode to round towards negative infinity.  If the
         * result is positive, behave as for {@code RoundingMode.DOWN};
         * if negative, behave as for {@code RoundingMode.UP}.  Note that
         * this rounding mode never increases the calculated value.
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode FLOOR Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code FLOOR} rounding
         *<tr align=right><td>5.5</td>  <td>5</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>1</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-2</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-3</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    FLOOR(BigDecimal.ROUND_FLOOR),

        /**
         * Rounding mode to round towards {@literal "nearest neighbor"}
         * unless both neighbors are equidistant, in which case round up.
         * Behaves as for {@code RoundingMode.UP} if the discarded
         * fraction is &ge; 0.5; otherwise, behaves as for
         * {@code RoundingMode.DOWN}.  Note that this is the rounding
         * mode commonly taught at school.
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode HALF_UP Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code HALF_UP} rounding
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>3</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-3</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    HALF_UP(BigDecimal.ROUND_HALF_UP),

        /**
         * Rounding mode to round towards {@literal "nearest neighbor"}
         * unless both neighbors are equidistant, in which case round
         * down.  Behaves as for {@code RoundingMode.UP} if the discarded
         * fraction is &gt; 0.5; otherwise, behaves as for
         * {@code RoundingMode.DOWN}.
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode HALF_DOWN Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code HALF_DOWN} rounding
         *<tr align=right><td>5.5</td>  <td>5</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-5</td>
         *</table>
         */
    HALF_DOWN(BigDecimal.ROUND_HALF_DOWN),

        /**
         * Rounding mode to round towards the {@literal "nearest neighbor"}
         * unless both neighbors are equidistant, in which case, round
         * towards the even neighbor.  Behaves as for
         * {@code RoundingMode.HALF_UP} if the digit to the left of the
         * discarded fraction is odd; behaves as for
         * {@code RoundingMode.HALF_DOWN} if it's even.  Note that this
         * is the rounding mode that statistically minimizes cumulative
         * error when applied repeatedly over a sequence of calculations.
         * It is sometimes known as {@literal "Banker's rounding,"} and is
         * chiefly used in the USA.  This rounding mode is analogous to
         * the rounding policy used for {@code float} and {@code double}
         * arithmetic in Java.
         *
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode HALF_EVEN Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code HALF_EVEN} rounding
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    HALF_EVEN(BigDecimal.ROUND_HALF_EVEN),

        /**
         * Rounding mode to assert that the requested operation has an exact
         * result, hence no rounding is necessary.  If this rounding mode is
         * specified on an operation that yields an inexact result, an
         * {@code ArithmeticException} is thrown.
         *<p>Example:
         *<table border>
         * <caption><b>Rounding mode UNNECESSARY Examples</b></caption>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with {@code UNNECESSARY} rounding
         *<tr align=right><td>5.5</td>  <td>throw {@code ArithmeticException}</td>
         *<tr align=right><td>2.5</td>  <td>throw {@code ArithmeticException}</td>
         *<tr align=right><td>1.6</td>  <td>throw {@code ArithmeticException}</td>
         *<tr align=right><td>1.1</td>  <td>throw {@code ArithmeticException}</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>throw {@code ArithmeticException}</td>
         *<tr align=right><td>-1.6</td> <td>throw {@code ArithmeticException}</td>
         *<tr align=right><td>-2.5</td> <td>throw {@code ArithmeticException}</td>
         *<tr align=right><td>-5.5</td> <td>throw {@code ArithmeticException}</td>
         *</table>
         */
    UNNECESSARY(BigDecimal.ROUND_UNNECESSARY);

    // Corresponding BigDecimal rounding constant
    final int oldMode;

    /**
     * Constructor
     *
     * @param oldMode The {@code BigDecimal} constant corresponding to
     *        this mode
     */
    private RoundingMode(int oldMode) {
        this.oldMode = oldMode;
    }

    /**
     * Returns the {@code RoundingMode} object corresponding to a
     * legacy integer rounding mode constant in {@link BigDecimal}.
     *
     * @param  rm legacy integer rounding mode to convert
     * @return {@code RoundingMode} corresponding to the given integer.
     * @throws IllegalArgumentException integer is out of range
     */
    public static RoundingMode valueOf(int rm) {
        switch(rm) {

        case BigDecimal.ROUND_UP:
            return UP;

        case BigDecimal.ROUND_DOWN:
            return DOWN;

        case BigDecimal.ROUND_CEILING:
            return CEILING;

        case BigDecimal.ROUND_FLOOR:
            return FLOOR;

        case BigDecimal.ROUND_HALF_UP:
            return HALF_UP;

        case BigDecimal.ROUND_HALF_DOWN:
            return HALF_DOWN;

        case BigDecimal.ROUND_HALF_EVEN:
            return HALF_EVEN;

        case BigDecimal.ROUND_UNNECESSARY:
            return UNNECESSARY;

        default:
            throw new IllegalArgumentException("argument out of range");
        }
    }
}
/*
 * Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.math;

/**
 * A class used to represent multiprecision integers that makes efficient
 * use of allocated space by allowing a number to occupy only part of
 * an array so that the arrays do not have to be reallocated as often.
 * When performing an operation with many iterations the array used to
 * hold a number is only increased when necessary and does not have to
 * be the same size as the number it represents. A mutable number allows
 * calculations to occur on the same number without having to create
 * a new number for every step of the calculation as occurs with
 * BigIntegers.
 *
 * Note that SignedMutableBigIntegers only support signed addition and
 * subtraction. All other operations occur as with MutableBigIntegers.
 *
 * @see     BigInteger
 * @author  Michael McCloskey
 * @since   1.3
 */

class SignedMutableBigInteger extends MutableBigInteger {

    /**
     * The sign of this MutableBigInteger.
     */
    int sign = 1;

    // Constructors

    /**
     * The default constructor. An empty MutableBigInteger is created with
     * a one word capacity.
     */
    SignedMutableBigInteger() {
        super();
    }

    /**
     * Construct a new MutableBigInteger with a magnitude specified by
     * the int val.
     */
    SignedMutableBigInteger(int val) {
        super(val);
    }

    /**
     * Construct a new MutableBigInteger with a magnitude equal to the
     * specified MutableBigInteger.
     */
    SignedMutableBigInteger(MutableBigInteger val) {
        super(val);
    }

    // Arithmetic Operations

    /**
     * Signed addition built upon unsigned add and subtract.
     */
    void signedAdd(SignedMutableBigInteger addend) {
        if (sign == addend.sign)
            add(addend);
        else
            sign = sign * subtract(addend);

    }

    /**
     * Signed addition built upon unsigned add and subtract.
     */
    void signedAdd(MutableBigInteger addend) {
        if (sign == 1)
            add(addend);
        else
            sign = sign * subtract(addend);

    }

    /**
     * Signed subtraction built upon unsigned add and subtract.
     */
    void signedSubtract(SignedMutableBigInteger addend) {
        if (sign == addend.sign)
            sign = sign * subtract(addend);
        else
            add(addend);

    }

    /**
     * Signed subtraction built upon unsigned add and subtract.
     */
    void signedSubtract(MutableBigInteger addend) {
        if (sign == 1)
            sign = sign * subtract(addend);
        else
            add(addend);
        if (intLen == 0)
            sign = 1;
    }

    /**
     * Print out the first intLen ints of this MutableBigInteger's value
     * array starting at offset.
     */
    public String toString() {
        return this.toBigInteger(sign).toString();
    }


}