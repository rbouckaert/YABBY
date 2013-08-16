package yabby.evolution.speciation;

// Arguably the most complex iterator I have ever written.

public class CalibrationLineagesIterator {
    // taxaPartialOrder[i] contains clades immediately contained in the i'th clade as indices (so, strictly smaller than i)
    final int[][] taxaPartialOrder;

    // Per calibration point, the number of taxa which is not below any other point.
    final int[] cladesFreeLins;

    // Use iterators 0 to nCurIters-1 (i.e. iters[0:nCurIters])
    private int nCurIters;

    // per clade Iterator
    private final LinsIterator[] iters;

    // last returned value from iterators: vals[i] for  iters[i]
    private int[][] vals;

    // Number of taxa not below any calibration point
    private int nFreeLineages;

    // indices of maximal clades
    private final int[] maximalClades;

    CalibrationLineagesIterator(final int[][] clades, final int[][] taxaPartialOrder,
                                final boolean[] maximal, final int leafCount) {
        cladesFreeLins = new int[clades.length];
        for(int k = 0; k < cladesFreeLins.length; ++k) {
            cladesFreeLins[k] = clades[k].length;
            for( final int l : taxaPartialOrder[k] ) {
                cladesFreeLins[k] -= clades[l].length;
            }
            assert cladesFreeLins[k] >= 0;
        }

        this.taxaPartialOrder = taxaPartialOrder;
        iters = new LinsIterator[clades.length+1];
        vals = new int[iters.length][];

        // number of maximal clades
        int nMax = 0;
        for(final boolean b : maximal) {
           nMax += b ? 1 : 0;
        }

        // indices of maximal clades in a list
        maximalClades = new int[nMax];
        nFreeLineages = leafCount;

        nMax = 0;
        for(int m = 0; m < maximal.length; ++m) {
            if( maximal[m] ) {
              maximalClades[nMax] = m;
              ++nMax;
              nFreeLineages -= clades[m].length;
            }
        }

        assert nFreeLineages >= 0;
    }

    // Prepare to iterate: ranks[i] gives the rank of the i'th clade. ranks is a permutation of (1,2,...,#points)
    int setup(final int[] ranks) {
        final int n = cladesFreeLins.length;

        // reset iterators used. each call to setOneIterator will increment it by one.
        nCurIters = 0;

        for(int k = 0; k < n; ++k) {
            setOneIterator(ranks, taxaPartialOrder[k], cladesFreeLins[k], ranks[k]);
        }

        if( nFreeLineages > 0 ) {
          setOneIterator(ranks, maximalClades, nFreeLineages, n+1);
        }
        
        for(int k = 0; k < nCurIters-1; ++k) {
            vals[k] = iters[k].next();
        }

        return nCurIters;
    }

    private void setOneIterator(final int[] ranks, final int[] joinerClades, final int nl, final int rank) {
        final int nSubs = joinerClades.length;

        LinsIterator itr/* = null*/;
        if( nSubs == 0 ) {
            itr = new LinsIterator(nl, rank, null);
        } else /*if( nl > 0 || nSubs > 2 ) */ {
            final int[] s = new int[nSubs];
            for(int i = 0; i < nSubs; ++i) {
                s[i] = ranks[joinerClades[i]];
            }
            itr = new LinsIterator(nl, rank, s);
        }

        //assert itr != null;
        //if( itr != null ) {
            // sorted according to rank
            iters[itr.rank-1] = itr;
            itr.startIter();
            ++nCurIters;
        //}
    }

    int[][] next()
    {
        final int[] l = iters[nCurIters-1].next();

        if( l != null ) {
            vals[nCurIters-1] = l;
            return vals;
        }

        int i = nCurIters-2;
        for( ; i >= 0; --i) {
            if( (vals[i] = iters[i].next()) != null) {
                break;
            }
        }

        if( i < 0 ) {
            return null;
        }

        ++i;

        for( ; i < nCurIters; ++i) {
            iters[i].startIter();
            vals[i] = iters[i].next();
        }

        return vals;
    }

    public int[][] allJoiners() {
        final int[][] joiners = new int[nCurIters][];

        for(int i = 0; i < nCurIters; ++i) {
            joiners[i] = iters[i].ljoins();
        }
        return joiners;
    }

    public int nStart(final int i) {
        return iters[i].nStart;
    }

    class LinsIterator {

        private final int rank;
        private final int nStart;
        private final int[] joiners;
        private final int[] aStart;
        // Current count of lineages at all relevant time points, from 0 (start) to clade top.
        private final int[] lins;
        private int lastJoinger;
        private boolean stopIter;

        LinsIterator(final int ns, final int r, final int[] jnr) {
            rank = r;
            nStart = ns;
            joiners = new int [r];

            lastJoinger = -1;

            // 2 for start+end, rank-1 intermediate levels
            aStart = new int [2 + rank-1];
            lins = new int [2 + rank-1];

            for(int k = 0; k < rank; ++k) {
                joiners[k] = 0;
            }

            if( jnr != null ) {
                for (final int j : jnr) {
                    joiners[j] = 1;
                    if (lastJoinger < j) {
                        lastJoinger = j;
                    }
                }
            }
            aStart[0] = ns;

            if( lastJoinger <= 0 ) {
                for(int i = 1; i < rank+1; ++i) {
                    aStart[i] = 2;
                }
                if( rank > 1 ) {
                    // first iteration increments this
                    aStart[rank-1] -= 1;
                }
            } else {
                //assert(rank > 1);

                if( nStart > 0 ) {
                    int i = 1;
                    for(; i < lastJoinger+1; ++i) {
                        aStart[i] = 1;
                    }
                    for(; i < rank+1; ++i) {
                        aStart[i] = 2;
                    }
                } else {
                    assert jnr != null;
                    int mj = jnr[0];
                    for (int aJnr : jnr) {
                        mj = Math.min(mj, aJnr);
                    }
                    int i = 1;
                    for(; i < mj+1; ++i) {
                        aStart[i] = 0;
                    }
                    for(; i < lastJoinger+1; ++i) {
                        aStart[i] = 1;
                    }
                    for(; i < rank+1; ++i) {
                        aStart[i] = 2;
                    }
                }
                // first iteration increments this
                aStart[rank-1] -= 1;
            }

        }

        void startIter() {
            for(int i = 0; i < rank+1; ++i) {
                lins[i] = aStart[i];
            }
            stopIter = false;
        }

        final int[] next()
        {
            int i = rank - 1;
            if( lastJoinger <= 0 ) {
                while( i >= 1 && lins[i] == lins[i-1]) {
                    --i;
                }
                if( i == 0 ) {
                    if( rank == 1 ) {
                        if( !stopIter ) {
                            stopIter = true;
                            return lins;
                        }
                    }
                    return null;
                }
                lins[i] += 1;
                ++i;
                while( i < rank ) {
                    lins[i] = 2;
                    ++i;
                }
            } else {

                while( i >= 1 && lins[i] == lins[i-1] + joiners[i-1] ) {
                    --i;
                }
                if( i == 0 ) {
                    return null;
                }
                lins[i] += 1;
                i++;
                while( i < rank ) {
                    lins[i] = (i <= lastJoinger) ? 1 : 2;
                    i++;
                }
            }
            return lins;
        }

        final int[] ljoins()  {
            return joiners;
        }
    }
}


