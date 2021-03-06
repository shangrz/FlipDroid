package flipdroid.grepper

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2/27/11
 * Time: 6:27 PM
 * To change this template use File | Settings | File Templates.
 */
class FruitFetcher {
    DetectionResult fetch(def htmlNode) {
        final def title = htmlNode.head.title.text()
        if (!title || title.trim().length() == 0) {
            title = htmlNode.html.head.title.text()
            htmlNode = htmlNode.html
        }
        def sameWithTitle
        def underscorePos = title.split("[_-]")
        final shortenedTitle
        if (underscorePos.size() == 1)
            shortenedTitle = title.trim()
        else {
            shortenedTitle = underscorePos[0].trim()
            if(shortenedTitle.indexOf("南方周末")!=-1){
                shortenedTitle = underscorePos[1].trim()
            }
        }

        Map headLines = htmlNode.body.depthFirst().toList().groupBy {
            if (it.text().trim() == shortenedTitle) {
                return "sameWithTitle"
            }

            if (it.name() == "h1" || it.name() == "h2" || it.name() == "h3") {
                return "heads"
            }
            if ((it.name() == "div" || it.name() == "p") && (it.@id.text().toUpperCase().equals('TITLE')
                    || it.@id.text().toUpperCase().startsWith('TITLE ')
                    || it.@id.text().toUpperCase().endsWith(' TITLE')
                    || it.@id.text().toUpperCase().indexOf(' TITLE ') != -1
                    || it.@class.text().toUpperCase().equals('TITLE')
                    || it.@class.text().toUpperCase().startsWith('TITLE ')
                    || it.@class.text().toUpperCase().endsWith(' TITLE')
                    || it.@class.text().toUpperCase().indexOf(' TITLE ') != -1)) {
                return "div-title"
            }
        }
        def sortedHeadlines
        def determinedTitle
        if (headLines['heads']) {
            sortedHeadlines = headLines['heads'].sort(
                    [compare: { o1, o2 ->
                        if (o1.name() < o2.name()) {
                            return -1
                        }
                        if (o1.name() == o2.name()) {
                            return 0
                        }
                        if (o1.name() > o2.name()) {
                            return 1
                        }

                    }] as Comparator)
        }
        DetectionResult result = new DetectionResult()

        determinedTitle = sortedHeadlines.find {it ->
            if (it.text().length() > title.length() / 3)
                return title.indexOf(it.text()) != -1
            return false
        }
        if (headLines['sameWithTitle']) {
            result.titleText = headLines['sameWithTitle'][0].text()
            result.titleNode = headLines['sameWithTitle'][0]

        } else if (determinedTitle) {
            result.titleText = determinedTitle.text()
            result.titleNode = determinedTitle

        } else if (sortedHeadlines && sortedHeadlines.size() != 0) {
            final underScore = title.indexOf('_')
            final pipe = title.indexOf('|')
            final minus = title.indexOf('-')
            underScore = underScore == -1 ? Integer.MAX_VALUE : underScore
            pipe = pipe == -1 ? Integer.MAX_VALUE : pipe
            minus = minus == -1 ? Integer.MAX_VALUE : minus

            final pos = Math.min(Math.min(underScore, pipe), minus)

            if (pos != Integer.MAX_VALUE)
                result.titleText = title[0..pos - 1]
            else
                result.titleText = title[0..-1]
            result.titleNode = determinedTitle

        }
        else if (headLines['div-title']) {
            determinedTitle = headLines['div-title'][0]
            result.titleText = determinedTitle.text()
            result.titleNode = determinedTitle

        } else if (!result.titleNode) {
            println result.titleText
            return result
        }
        return result

//        boolean bodyDetermined = false
        //        def tempText = result.titleText
        //        groovy.util.slurpersupport.NodeChild foundNode = result.titleNode
        //        def nodeLength = foundNode.text().length()
        //        String bodyTextPipe
        //        while (!bodyDetermined) {
        //
        //            def parentNode = foundNode.parent()
        //            if (foundNode == parentNode)
        //                break;
        //            foundNode = parentNode
        //
        //            def parentNodeLength = foundNode.text().length()
        //
        //
        //            if ((parentNodeLength / nodeLength) > 10) {
        //
        ////                StringWriter sw = new StringWriter();
        ////                PrintWriter pw = new PrintWriter(sw);
        ////                XmlNodePrinter nodePrinter = new XmlNodePrinter(pw);
        ////                groovy.util.slurpersupport.Node current = foundNode.nodeIterator().next()
        //
        //                def nodeText = new StreamingMarkupBuilder().bind {
        //                     mkp.yield foundNode.parent()
        //                }
        //                //nodePrinter.print(current.)
        //
        //                final byte[] data = (nodeText as String).bytes
        //
        //
        //                final BoilerpipeExtractor extractor = CommonExtractors.ARTICLE_EXTRACTOR;
        //                final HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance();
        //                def cs
        //                try {
        //                    cs = Charset.forName("utf-8");
        //                } catch (UnsupportedCharsetException e) {
        //                    // keep default
        //                }
        //
        //                bodyTextPipe = hh.process(new HTMLDocument(data, cs), extractor);
        ////                foundNode = foundNode.children().list().max {it ->
        ////                    if (it.name() == "style" || it.name() == "script")
        ////                        return 0
        ////
        ////                    final length = it.filteredText().length()
        ////                    return length
        ////                }
        //                bodyDetermined = true
        //            }
        //        }
        //
        //        if (!bodyDetermined) {
        //            return result
        //        } else {
        //            result.bodyNode = foundNode
        //            result.bodyText = bodyTextPipe
        //
        //        }

    }


}
