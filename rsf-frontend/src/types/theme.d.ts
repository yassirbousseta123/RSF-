import '@mui/material/styles';
import { Theme } from '@mui/material/styles';

declare module '@mui/material/styles' {
  interface TypographyVariants {
    caption2: React.CSSProperties;
  }

  interface TypographyVariantsOptions {
    caption2?: React.CSSProperties;
  }

  interface PaletteColor {
    lighter?: string;
    darker?: string;
  }
  
  interface SimplePaletteColorOptions {
    lighter?: string;
    darker?: string;
  }
} 