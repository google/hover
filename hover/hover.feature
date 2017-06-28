Feature: Hover Menu

  #------- General ------
  Scenario: Overlay permission screen opens on M+
    Given the app is on Android M or greater
      And that the app does not have overlay permission
    When the app sends an intent to display the overlay permission screen
    Then the overlay permission screen is displayed

  Scenario: Overlay window on O+
    Given the app is on Android O or greater
    When Hover is launched
    Then Hover is in the "application overlay" window

  Scenario: Run as foreground Service
    Given Hover is being run in a HoverMenuService
    When a non-null Notification is provided by the subclass
    Then HoverMenuService runs as a foreground Service
      And the given Notification is displayed in the notification shade

  #------- Closed Menu ----------
  # NOT IMPLEMENTED AS OF 0.9.8, BUT IS DESIRED.
  Scenario: No overlay exists when Hover is in the closed state.
    Given that Hover is in the closed state
    Then nothing is visible on screen
      And there is no View on the screen (not even invisible Views)

  #------- Collapsed Menu -------
  Scenario: Hover docks to predefined location upon first launch
    Given that Hover has never been launched
    When Hover is launched
      And Hover is collapsed
    Then Hover docks to its default dock location

  Scenario: Hover docks to saved location after first launch
    Given that Hover has been launched previously
      And Hover is not currently launched
    When Hover is launched
      And Hover is collapsed
    Then Hover docks to the same position as the last time it ran

  Scenario: Hover can be dragged by the user
    Given that Hover is launched
      And Hover is collapsed
    When the user touches Hover and drags
    Then Hover's floating tab follows the user's finger

  Scenario: Hover docks to the nearest vertical screen edge
    Given that Hover is launched
      And Hover is collapsed
      And Hover is being dragged
    When the user releases Hover's floating tab outside the exit area
    Then Hover's floating tab docks to the nearest vertical screen edge

  Scenario: Exit location is visible while dragging
    Given that Hover is launched
      And Hover is collapsed
    When the user drags Hover's floating tab
    Then Hover's exit area is displayed

  Scenario: Exit location disappears when dragging ends away from exit
    Given that Hover is launched
      And Hover is collapsed
      And Hover is being dragged
    When the user releases Hover's floating tab outside the exit area
    Then Hover's exit area is not displayed

  Scenario: Hover closes when floating tab is dropped on exit
    Given that Hover is launched
      And Hover is collapsed
      And Hover is being dragged
    When the user releases Hover's floating tab in the exit area
    Then Hover closes

  Scenario: Hover changes floating tab when the selected section disappears
    Given that Hover is launched
      And Hover is collapsed
    When the selected section is removed
      And there is at least 1 other section in the Hover menu
    Then Hover selects another section and updates the floating tab

  Scenario: Hover changes floating tab when the selected section changes
    Given that Hover is launched
      And Hover is collapsed
    When the selected section changes
    Then Hover updates the floating tab to the newly selected section

  Scenario: Hover expands menu when collapsed floating tab is tapped
    Given that Hover is launched
      And Hover is collapsed
    When the user taps Hover's floating tab
    Then Hover expands its menu
      And the selected section is displayed in the expanded menu

  Scenario: Hover re-opens with previously selected tab
    Given that Hover is launched
      And Hover is collapsed
      And Hover is presenting MenuX
      And Hover's floating tab is TabX
    When Hover is destroyed
      And Hover is launched
      And Hover is collapsed
      And Hover is presenting MenuX
    Then Hover's floating tab is TabX

  #-------- Expanded Menu ---------
  Scenario: Hover changes selection when new tab tapped
    Given that Hover is launched
      And Hover is expanded
      And Hover has more than 1 section
    When the user taps an unselected section
    Then Hover selects the tapped section
      And Hover displays the tapped section's content

  Scenario: Hover changes selection when it is changed programmatically
    Given that Hover is launched
      And Hover is expanded
      And Hover has more than 1 section
    When Hover is instructed to select a new section in code
    Then Hover selects the desired section
      And Hover displays the desired section's content

  Scenario: Hover changes selection when selected section is removed
    Given that Hover is launched
      And Hover is expanded
      And Hover has more than 1 section
    When the selected section is removed
    Then the selected tab disappears
      And Hover selects another section
      And Hover displays the newly selected section's content

  Scenario: Hover adds tabs when new section is added
    Given that Hover is launched
      And Hover is expanded
    When a new section is added
    Then a new tab is displayed for the new section

  Scenario: Hover removes tab when non-selected section is removed
    Given that Hover is launched
      And Hover is expanded
      And Hover has more than 1 section
    When an un-selected section is removed
    Then the removed sections tab disappears

  Scenario: Hover switches tab positions
    Given that Hover is launched
      And Hover is expanded
      And Hover has at least 2 sections
    When sections change their order
    Then Hover's tabs change position to match the new section order

  Scenario: Hover collapses when selected tab is tapped
    Given that Hover is launched
      And Hover is expanded
    When the user taps the selected tab
    Then Hover collapses its menu

  Scenario: Hover collapses when user presses back
    Given that Hover is launched
      And Hover is expanded
      And Hover's content has no backstack
    When the user presses the hardware back button
    Then Hover collapses its menu

  Scenario: Hover displays selected tab when it collapses
    Given that Hover is launched
      And Hover is expanded
      And tab X is selected
    When Hover collapses
    Then tab X is displayed as the Hover's floating tab

  Scenario: Hover returns to dock after collapse
    Given that Hover is launched
      And Hover is expanded
    When Hover collapses
    Then Hover docks at the same place as before it expanded

  #-------- Cross-State Behavior --------
  Scenario: Hover closes when no menu sections available
    Given that Hover is launched
    And Hover is <state>
    When an empty menu is set on Hover
    Then Hover closes

    Examples:
    |   state   |
    | collapsed |
    |  expanded |