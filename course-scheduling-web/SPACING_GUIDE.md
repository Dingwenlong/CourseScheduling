# Spacing System Specification

This project uses a 4px-based spacing scale to ensure visual consistency across all pages and components.

## Spacing Scale

| Variable | Value | Description |
| :--- | :--- | :--- |
| `--spacing-xs` | 4px | Smallest gaps, tight icon/text spacing |
| `--spacing-sm` | 8px | Small component internal spacing |
| `--spacing-md` | 12px | Standard component gap, small card padding |
| `--spacing-lg` | 16px | Page content padding, standard card padding |
| `--spacing-xl` | 24px | Large section gaps, modal header padding |
| `--spacing-2xl` | 32px | Major layout sections, bottom padding |
| `--spacing-3xl` | 48px | Hero sections, large empty states |
| `--spacing-4xl` | 64px | Extremely large display spacing |

## Utility Classes

We provide a comprehensive set of utility classes for margins and paddings.

### Margins
- `.m-{size}`: All sides
- `.mt-{size}`: Top
- `.mb-{size}`: Bottom
- `.ml-{size}`: Left
- `.mr-{size}`: Right
- `.mx-{size}`: Horizontal
- `.my-{size}`: Vertical
- `.mx-auto`: Centering

*Available sizes: 0, 4, 8, 12, 16, 24, 32*

### Paddings
- `.p-{size}`: All sides
- `.pt-{size}`: Top
- `.pb-{size}`: Bottom
- `.pl-{size}`: Left
- `.pr-{size}`: Right
- `.px-{size}`: Horizontal
- `.py-{size}`: Vertical

*Available sizes: 0, 4, 8, 12, 16, 24, 32*

### Gaps (Flex/Grid)
- `.gap-4`, `.gap-8`, `.gap-12`, `.gap-16`

## Usage Rules

1. **Avoid Hardcoded Pixels**: Never use `margin: 15px`. Use `var(--spacing-md)` (12px) or `var(--spacing-lg)` (16px).
2. **Consistency**:
   - Card Padding: Use `var(--spacing-lg)` (16px).
   - Card Gap: Use `var(--spacing-md)` (12px).
   - Section Title Bottom Margin: Use `var(--spacing-md)` (12px).
   - Form Field Gap: Use `var(--spacing-md)` (12px).
3. **Responsive Spacing**: Use media queries to adjust spacing for larger screens if necessary, but stay within the defined scale.
