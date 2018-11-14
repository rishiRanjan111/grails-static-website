package org.grails

import groovy.json.JsonSlurper
import groovy.time.TimeCategory
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.model.Guide

import java.text.DateFormat
import java.text.SimpleDateFormat

@CompileStatic
class GuidesFetcher {

    public static final String GUIDES_JSON = 'https://raw.githubusercontent.com/grails/grails-guides/gh-pages/guides.json'

    @CompileDynamic
    static List<Guide> fetchGuides(boolean skipFuture = true) {
        URL url = new URL(GUIDES_JSON)
        def jsonArr = new JsonSlurper().parseText(url.text)
        DateFormat dateFormat = new SimpleDateFormat('dd MMM yyyy', Locale.US)
        List<Guide> guides = jsonArr.collect {
            Guide guide = new Guide(authors: it.authors as List,
                    category: it.category,
                    githubSlug: it.githubSlug,
                    name: it.name,
                    title: it.title,
                    subtitle: it.subtitle,
                    tags: it.tags as List
            )
            if ( it.publicationDate ) {
                guide.publicationDate = dateFormat.parse(it.publicationDate as String)
            }
            guide
        }
        if(skipFuture) {
            guides = guides.findAll { it.publicationDate.before(tomorrow()) }
        }
        guides.sort { Guide a, Guide b ->
            b.publicationDate <=> a.publicationDate
        }
    }

    @CompileDynamic
    static Date tomorrow() {
        Date tomorrow = new Date()
        use(TimeCategory) {
            tomorrow = tomorrow += 1.day
        }
        tomorrow
    }
}