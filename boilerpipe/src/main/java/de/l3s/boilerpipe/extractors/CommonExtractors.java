package de.l3s.boilerpipe.extractors;

import de.l3s.boilerpipe.BoilerpipeExtractor;

/**
 * Provides quick access to common {@link BoilerpipeExtractor}s.
 * 
 * @author Christian Kohlschütter
 */
public final class CommonExtractors {
	private CommonExtractors() {
	}

	/**
	 * Works very well for most types of Article-like HTML.
	 */
	public static final ArticleExtractor ARTICLE_EXTRACTOR = ArticleExtractor.INSTANCE;

    public static final ChineseArticleExtractor CHINESE_ARTICLE_EXTRACTOR = ChineseArticleExtractor.INSTANCE;

	/**
	 * Usually worse than {@link ArticleExtractor}, but simpler/no heuristics.
	 */
	public static final DefaultExtractor DEFAULT_EXTRACTOR = DefaultExtractor.INSTANCE;

	/**
	 * Trained on krdwrd Canola (different definition of "boilerplate"). You may
	 * give it a try.
	 */
	public static final CanolaExtractor CANOLA_EXTRACTOR = CanolaExtractor.INSTANCE;

	/**
	 * Dummy Extractor; should return the input text. Use this to double-check
	 * that your problem is within a particular {@link BoilerpipeExtractor}, or
	 * somewhere else.
	 */
	public static final KeepEverythingExtractor KEEP_EVERYTHING_EXTRACTOR = KeepEverythingExtractor.INSTANCE;
}
