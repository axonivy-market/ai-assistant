package com.axonivy.utils.aiassistant.validator;

import java.net.MalformedURLException;
import java.net.URI;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.portal.components.util.FacesMessageUtils;

import ch.ivyteam.ivy.environment.Ivy;

@FacesValidator("urlValidator")
public class UrlValidator implements Validator {

  @Override
  public void validate(FacesContext context, UIComponent component,
      Object value) throws ValidatorException {
    String strValue = StringUtils.defaultIfBlank(value.toString(), null);

    try {
      if (StringUtils.isBlank(strValue)) {
        return;
      }

      URI.create(strValue).toURL();
    } catch (MalformedURLException | IllegalArgumentException e) {
      FacesMessage message = FacesMessageUtils.sanitizedMessage(
          FacesMessage.SEVERITY_ERROR,
          Ivy.cms().co("/Labels/Message/InvalidUrl"), null);
      throw new ValidatorException(message);
    }
  }
}