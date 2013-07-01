package org.estatio.dom.agreement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.query.Query;
import org.apache.isis.core.commons.matchers.IsisMatchers;

import org.estatio.dom.FinderInteraction;
import org.estatio.dom.FinderInteraction.FinderMethod;

public class AgreementTypesTest_finders {

    private FinderInteraction finderInteraction;

    private AgreementTypes agreements;

    @Before
    public void setup() {
        
        agreements = new AgreementTypes() {

            @Override
            protected <T> T firstMatch(Query<T> query) {
                finderInteraction = new FinderInteraction(query, FinderMethod.FIRST_MATCH);
                return null;
            }
            @Override
            protected List<AgreementType> allInstances() {
                finderInteraction = new FinderInteraction(null, FinderMethod.ALL_INSTANCES);
                return null;
            }

            @Override
            protected <T> List<T> allMatches(Query<T> query) {
                finderInteraction = new FinderInteraction(query, FinderMethod.ALL_MATCHES);
                return null;
            }
        };
    }


    @Test
    public void findAgreementByTitle() {

        agreements.find("Some.exact*title");
        
        // then
        assertThat(finderInteraction.getFinderMethod(), is(FinderMethod.FIRST_MATCH));
        assertThat(finderInteraction.getResultType(), IsisMatchers.classEqualTo(AgreementType.class));
        assertThat(finderInteraction.getQueryName(), is("findByTitle"));
        assertThat(finderInteraction.getArgumentsByParameterName().get("title"), is((Object)"Some.exact*title"));
        assertThat(finderInteraction.getArgumentsByParameterName().size(), is(1));
    }
    

}