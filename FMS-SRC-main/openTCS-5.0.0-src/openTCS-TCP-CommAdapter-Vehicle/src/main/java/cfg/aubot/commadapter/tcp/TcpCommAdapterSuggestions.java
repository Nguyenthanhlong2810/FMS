package cfg.aubot.commadapter.tcp;

import org.opentcs.components.plantoverview.PropertySuggestions;
import org.opentcs.example.VehicleProperties;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class TcpCommAdapterSuggestions implements PropertySuggestions {

  private final I18nVehicleTcpConnectionInfo i18n;

  private final Set<String> keySuggestions = new HashSet<>();
  private final Set<String> valueSuggestions = new HashSet<>();

  @Inject
  public TcpCommAdapterSuggestions(I18nVehicleTcpConnectionInfo i18n) {
    this.i18n = requireNonNull(i18n, "I18nVehicleTcpConnectionInfo");
    keySuggestions.add(i18n.host);
    keySuggestions.add(i18n.port);
  }

  /**
   * Returns suggested property keys.
   *
   * @return Suggested property keys.
   */
  @Nonnull
  @Override
  public Set<String> getKeySuggestions() {
    return keySuggestions;
  }

  /**
   * Returns suggested property values.
   *
   * @return Suggested property values.
   */
  @Nonnull
  @Override
  public Set<String> getValueSuggestions() {
    return valueSuggestions;
  }

  /**
   * Returns suggested property values that are specified for the <Code>key</Code>.
   *
   * @param key A key suggestion for which value suggestions are requested.
   * @return A set of property value suggestions.
   */
  @Override
  public Set<String> getValueSuggestionsFor(String key) {
    if (key.equalsIgnoreCase(VehicleProperties.PROPKEY_VEHICLE_PORT)) {
      Set<String> values = new HashSet<>();
      values.add(String.valueOf(i18n.DEFAULT_PORT));

      return values;
    }

    return valueSuggestions;
  }
}
