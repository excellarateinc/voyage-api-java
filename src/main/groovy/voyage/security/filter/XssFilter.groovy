package voyage.security.filter

import org.owasp.esapi.ESAPI
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse
import java.util.regex.Pattern

/**
 * Servlet filter that parses all incoming request parameters and header values for malicious XSS attacks. Any request
 * content that matches the XSS PATTERNS are removed from the request before they are accessed by the API. Additionally,
 * this filter will encode all incoming data by default to avoid encoding attacks for HTML, CSS, JS, SQL, VBScript, OS,
 * LDAP, XPATH, XML, URL.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class XssFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(XssFilter)

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        LOG.debug('XssServletFilter: Wrapping the inbound request with the XSSRequestWrapper.')
        chain.doFilter(getXssRequestWrapperInstance((HttpServletRequest) request), response)
    }

    XssRequestWrapper getXssRequestWrapperInstance(HttpServletRequest request) {
        return new XssRequestWrapper(request)
    }

    class XssRequestWrapper extends HttpServletRequestWrapper {
        private static final Pattern[] PATTERNS = [
                // Script fragments
                Pattern.compile('<script>(.*?)</script>', Pattern.CASE_INSENSITIVE),
                // src='...'
                Pattern.compile('src[\r\n]*=[\r\n]*\'(.*?)\'', Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
                Pattern.compile('src[\r\n]*=[\r\n]*\"(.*?)\"', Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
                // lonely script tags
                Pattern.compile('</script>', Pattern.CASE_INSENSITIVE),
                Pattern.compile('<script(.*?)>', Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
                // eval(...)
                Pattern.compile('eval\\((.*?)\\)', Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
                // expression(...)
                Pattern.compile('expression\\((.*?)\\)', Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
                // javascript:...
                Pattern.compile('javascript:', Pattern.CASE_INSENSITIVE),
                // vbscript:...
                Pattern.compile('vbscript:', Pattern.CASE_INSENSITIVE),
                // onload(...)=...
                Pattern.compile('onload(.*?)=', Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        ]

        XssRequestWrapper(HttpServletRequest servletRequest) {
            super(servletRequest)
        }

        @Override
        String[] getParameterValues(String parameter) {
            String[] values = super.getParameterValues(parameter)

            if (!values) {
                return []
            }

            int count = values.length
            String[] encodedValues = new String[count]
            for (int i = 0; i < count; i++) {
                encodedValues[i] = stripXSS(values[i])
            }

            return encodedValues
        }

        @Override
        String getParameter(String parameter) {
            String value = super.getParameter(parameter)
            return stripXSS(value)
        }

        @Override
        String getHeader(String name) {
            String value = super.getHeader(name)
            return stripXSS(value)
        }

        private static String stripXSS(String value) {
            if (value) {
                // Avoid encoded attacks by parsing the value using ESAPI
                String encodedValue = ESAPI.encoder().canonicalize(value)

                // Avoid null characters
                encodedValue = encodedValue.replaceAll('', '')

                // Remove all sections that match a pattern
                for (Pattern pattern : PATTERNS) {
                    encodedValue = pattern.matcher(encodedValue).replaceAll('')
                }

                return encodedValue
            }
            return value
        }
    }
}
