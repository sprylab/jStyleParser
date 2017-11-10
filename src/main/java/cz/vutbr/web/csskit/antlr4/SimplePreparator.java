package cz.vutbr.web.csskit.antlr4;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import cz.vutbr.web.Config;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.MediaQuery;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RuleFontFace;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.RuleMedia;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.RuleViewport;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.PseudoPage;

public class SimplePreparator implements Preparator {
	protected static final Logger log = LoggerFactory
			.getLogger(SimplePreparator.class);

	private static RuleFactory rf = CSSFactory.getRuleFactory();

	private Element elem;
	private boolean inlinePriority;

	public SimplePreparator(Element e, boolean inlinePriority) {
		this.elem = e;
		this.inlinePriority = inlinePriority;
	}

	public RuleBlock<?> prepareRuleSet(List<CombinedSelector> cslist,
			List<Declaration> dlist, boolean wrap, List<MediaQuery> media) {

        // check emptiness
        if ((cslist == null || cslist.isEmpty())
            || (dlist == null || dlist.isEmpty())) {
            if (Config.LOGGING_ENABLED && log.isDebugEnabled()) {
                log.debug("Empty RuleSet was ommited");
            }
            return null;
        }

        // create rule set
        RuleSet rs = rf.createSet();
        rs.setSelectors(cslist);
        rs.replaceAll(dlist);
        if (Config.LOGGING_ENABLED && log.isInfoEnabled()) {
            log.info("Created RuleSet as with:\n{}", rs);
        }

        // wrap
        if (wrap) {
            // swap numbers, so RuleMedia is created before RuleSet
            RuleMedia rm = rf.createMedia();
            if (Config.LOGGING_ENABLED && log.isDebugEnabled()) {
                log.debug("Wrapping RuleSet {} into RuleMedia: {}", rs, media);
            }

            rm.unlock();
            rm.add(rs);
            rm.setMediaQueries(media);

            // return wrapped block
            return (RuleBlock<?>) rm;
        }

        // return classic rule set
        return (RuleBlock<?>) rs;
    }

	public RuleBlock<?> prepareRuleMedia(List<RuleSet> rules, List<MediaQuery> media) {

        if (rules == null || rules.isEmpty()) {
            if (Config.LOGGING_ENABLED && log.isDebugEnabled()) {
                log.debug("Empty RuleMedia was ommited");
            }
            return null;
        }

        // create media at position of mark
        RuleMedia rm = rf.createMedia();
        rm.replaceAll(rules);
        if (media != null && !media.isEmpty())
            rm.setMediaQueries(media);

        if (Config.LOGGING_ENABLED && log.isInfoEnabled()) {
            log.info("Create @media as with:\n{}", rm);
        }

        return (RuleBlock<?>) rm;
    }

	public RuleBlock<?> prepareRulePage(List<Declaration> declarations, List<RuleMargin> marginRules, String name, String pseudo) {

        if ((declarations == null || declarations.isEmpty()) &&
            (marginRules == null || marginRules.isEmpty())) {
            if (Config.LOGGING_ENABLED && log.isDebugEnabled()) {
                log.debug("Empty RulePage was ommited");
            }
            return null;
        }

        RulePage rp = rf.createPage();
        if (declarations != null)
            for (Declaration d : declarations)
                rp.add(d);
        if (marginRules != null)
            for (RuleMargin m : marginRules)
                rp.add(m);
        rp.setName(name);

        rp.setPseudo(pseudo);
        if (Config.LOGGING_ENABLED && log.isInfoEnabled()) {
            log.info("Create @page as with:\n{}", rp);
        }

        return (RuleBlock<?>) rp;
    }

    public RuleMargin prepareRuleMargin(String area, List<Declaration> decl) {

        if ((decl == null || decl.isEmpty()))
        {
            if (Config.LOGGING_ENABLED && log.isDebugEnabled()) {
                log.debug("Empty RuleMargin was ommited");
            }
            return null;
        }

        RuleMargin rm = rf.createMargin(area);
        rm.replaceAll(decl);

        if (Config.LOGGING_ENABLED && log.isInfoEnabled()) {
            log.info("Create @" + area + " with:\n" + rm);
        }

        return rm;
    }
	
    public RuleBlock<?> prepareRuleViewport(List<Declaration> decl) {

        if (decl == null || decl.isEmpty()) {
            if (Config.LOGGING_ENABLED && log.isDebugEnabled()) {
                log.debug("Empty Viewport was ommited");
            }
            return null;
        }

        RuleViewport rp = rf.createViewport();
        rp.replaceAll(decl);
        if (Config.LOGGING_ENABLED && log.isInfoEnabled()) {
            log.info("Create @viewport as {}th with:\n{}", rp);
        }

        return (RuleBlock<?>) rp;
    }

    public RuleBlock<?> prepareRuleFontFace(List<Declaration> decl) {

        if (decl == null || decl.isEmpty()) {
            if (Config.LOGGING_ENABLED && log.isDebugEnabled()) {
                log.debug("Empty RuleFontFace was ommited");
            }
            return null;
        }

        RuleFontFace rp = rf.createFontFace();
        rp.replaceAll(decl);
        if (Config.LOGGING_ENABLED && log.isInfoEnabled()) {
            log.info("Create @font-face as with:\n{}", rp);
        }

        return (RuleBlock<?>) rp;
    }

	public RuleBlock<?> prepareInlineRuleSet(List<Declaration> dlist,
			List<PseudoPage> pseudos) {

        if (dlist == null || dlist.isEmpty()) {
            if (Config.LOGGING_ENABLED && log.isDebugEnabled()) {
                log.debug("Empty RuleSet (inline) was ommited");
            }
            return null;
        }

        // create selector with element
        CombinedSelector cs = (CombinedSelector) rf.createCombinedSelector()
            .unlock();
        Selector sel = (Selector) rf.createSelector().unlock();
        sel.add(rf.createElementDOM(elem, inlinePriority));
        if (pseudos != null) sel.addAll(pseudos);
        cs.add(sel);

        RuleSet rs = rf.createSet();
        rs.replaceAll(dlist);
		rs.setSelectors(Arrays.asList(cs));

        if (Config.LOGGING_ENABLED && log.isInfoEnabled()) {
            log.info("Create @media as with:\n{}", rs);
        }

        return (RuleBlock<?>) rs;
    }

}
